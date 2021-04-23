package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.sun.jna.NativeLibrary;
import com.sun.jna.StringArray;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.LibC;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.binding.internal.libvlc_instance_t;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PriorityQueue;
import java.util.Queue;

import static uk.co.caprica.vlcj.binding.LibVlc.libvlc_new;
import static uk.co.caprica.vlcj.binding.LibVlc.libvlc_release;

public final class VLCUtilities {

  private static final String VLC_PLUGIN_PATH;
  private static Path NATIVE_VLC_PATH;

  static {
    VLC_PLUGIN_PATH = "VLC_PLUGIN_PATH";
  }

  private VLCUtilities() {}

  /**
   * Checks if VLC installation exists or not.
   *
   * @param directory the library
   * @return whether vlc can be found or not
   */
  public static boolean checkVLCExistence(@NotNull final File directory) {
    if (!directory.exists()) {
      return false;
    }
    final NativeDiscovery discovery = new NativeDiscovery();
    if (discovery.discover()) {
      NATIVE_VLC_PATH = Paths.get(discovery.discoveredPath());
      return true;
    }
    final String extension =
        RuntimeUtilities.isWindows() ? "dll" : RuntimeUtilities.isMac() ? "dylib" : "so";
    final String keyword = "libvlc." + extension;
    boolean plugins = false;
    boolean libvlc = false;
    final Queue<File> folders = getPriorityQueue(keyword);
    folders.add(directory);
    while (!folders.isEmpty()) {
      if (plugins && libvlc) {
        return true;
      }
      final File f = folders.remove();
      final String name = f.getName();
      final String path = f.getAbsolutePath();
      if (f.isDirectory()) {
        if (!plugins && name.equals("plugins")) {
          setVLCPluginPath(
              RuntimeUtilities.isWindows()
                  ? f.getParent()
                  : RuntimeUtilities.isMac()
                      ? f.getParent() + "/lib/../plugins"
                      : f.getParent() + "/vlc/plugins");
          Logger.info(String.format("Found Plugins Path (%s)", path));
          plugins = true;
        } else {
          final File[] children = f.listFiles();
          if (children != null) {
            for (final File child : children) {
              if (child.isDirectory() || (child.isFile() && child.getName().contains(extension))) {
                folders.add(child);
              }
            }
          }
        }
      } else {
        if (!libvlc && name.equals(keyword)) {
          NATIVE_VLC_PATH = f.getParentFile().toPath();
          final String vlcPath = NATIVE_VLC_PATH.toAbsolutePath().toString();
          if (RuntimeUtilities.isMac()) {
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcCoreLibraryName(), vlcPath);
            NativeLibrary.getInstance(RuntimeUtil.getLibVlcCoreLibraryName());
          }
          NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath);
          Logger.info(String.format("Found LibVLC (%s)", path));
          if (loadLibVLCLibrary()) {
            Logger.info("Successfully Loaded LibVLC Library");
          }
          libvlc = true;
        }
      }
    }
    return false;
  }

  private static PriorityQueue<File> getPriorityQueue(@NotNull final String keyword) {
    return RuntimeUtilities.isWindows()
        ? new PriorityQueue<>()
        : new PriorityQueue<>(
            (o1, o2) -> {

              /*

              Heuristic algorithm which allows the libvlc compiled file be found easier on Unix
              systems. (This includes Mac and Linux, where the file is deeply in the recursion).
              It is not needed for Windows as the file is in the main directory.

               */

              final String name = o1.getName();
              if (name.equals(keyword) || name.equals("lib")) {
                return Integer.MIN_VALUE;
              }
              return o1.compareTo(o2);
            });
  }

  /**
   * Sets the VLC plugin path to the specified path provided.
   *
   * @param path the vlc plugin path
   */
  private static void setVLCPluginPath(@NotNull final String path) {
    final String env = System.getenv("VLC_PLUGIN_PATH");
    if (env == null || env.length() == 0) {
      if (RuntimeUtilities.isWindows()) {
        LibC.INSTANCE._putenv(String.format("%s=%s", VLC_PLUGIN_PATH, path));
      } else {
        LibC.INSTANCE.setenv(VLC_PLUGIN_PATH, path, 1);
      }
    }
  }

  /**
   * Loads the LibVLC library of VLC.
   *
   * @return whether if the library was successfully loaded or not.
   */
  private static boolean loadLibVLCLibrary() {
    try {
      final libvlc_instance_t instance = libvlc_new(0, new StringArray(new String[0]));
      if (instance != null) {
        libvlc_release(instance);
        final LibVlcVersion version = new LibVlcVersion();
        if (version.isSupported()) {
          return true;
        }
      }
    } catch (final UnsatisfiedLinkError e) {
      Logger.info(e.getMessage());
    }
    return false;
  }

  /**
   * Gets the Native path of VLC binaries.
   *
   * @return the File of the folder
   */
  public static Path getNativeVlcPath() {
    return NATIVE_VLC_PATH;
  }
}
