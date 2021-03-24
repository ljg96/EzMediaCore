/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/2/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.relocation.JarRelocator;
import com.github.pulsebeat02.minecraftmedialibrary.relocation.Relocation;
import com.github.pulsebeat02.minecraftmedialibrary.utility.DependencyUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is the full dependency management class, which handles the dependencies of the project by
 * managing them. It first creates a folder if it cannot be found. Then, it creates a relocation
 * folder for the relocated jars. After that, the methods are up to the user to decide when to
 * install, load, or relocate the binaries.
 */
public class DependencyManagement {

  private final Set<File> files;
  private final File dir;
  private final File relocatedDir;

  /**
   * Instantiates a new DependencyManagement.
   *
   * @param library instance
   */
  public DependencyManagement(@NotNull final MinecraftMediaLibrary library) {
    files = new HashSet<>();
    dir = new File(library.getDependenciesFolder());
    if (!dir.exists()) {
      if (dir.mkdir()) {
        Logger.info(
            "Dependency Directory ("
                + dir.getAbsolutePath()
                + ") does not exist... Creating a folder");
      } else {
        Logger.info("Dependency Directory (" + dir.getAbsolutePath() + ") exists!");
      }
    }
    relocatedDir = new File(dir + File.separator + "/relocated");
    if (!relocatedDir.exists()) {
      if (relocatedDir.mkdir()) {
        Logger.info(
            "Relocated Directory ("
                + relocatedDir.getAbsolutePath()
                + ") does not exist... Creating a folder");
      } else {
        Logger.info("Relocated Directory (" + relocatedDir.getAbsolutePath() + ") exists!");
      }
    }
  }

  /**
   * Instantiates a new DependencyManagement.
   *
   * @param dirPath directory
   */
  public DependencyManagement(@NotNull final String dirPath) {
    files = new HashSet<>();
    dir = new File(dirPath);
    if (!dir.exists()) {
      if (dir.mkdir()) {
        Logger.info(
            "Dependency Directory ("
                + dir.getAbsolutePath()
                + ") does not exist... Creating a folder");
      } else {
        Logger.info("Dependency Directory (" + dir.getAbsolutePath() + ") exists!");
      }
    }
    relocatedDir = new File(dir + File.separator + "/relocated");
    if (!relocatedDir.exists()) {
      if (relocatedDir.mkdir()) {
        Logger.info(
            "Relocated Directory ("
                + relocatedDir.getAbsolutePath()
                + ") does not exist... Creating a folder");
      } else {
        Logger.info("Relocated Directory (" + relocatedDir.getAbsolutePath() + ") exists!");
      }
    }
  }

  /** Installs all libraries from links. */
  public void install() {
    for (final RepositoryDependency dependency : RepositoryDependency.values()) {
      if (!checkExists(dir, dependency)) {
        final String artifact = dependency.getArtifact();
        File file = null;
        if (dependency.getResolution() == DependencyResolution.MAVEN_DEPENDENCY) {
          Logger.info("Checking Maven Central Repository for " + artifact);
          try {
            file = DependencyUtilities.downloadMavenDependency(dependency, dir.getAbsolutePath());
          } catch (final IOException e) {
            Logger.info("Could NOT find " + artifact + " in Maven Central Repository!");
            e.printStackTrace();
          }
        } else if (dependency.getResolution() == DependencyResolution.JITPACK_DEPENDENCY) {
          Logger.info("Checking Jitpack Central Repository for " + artifact);
          try {
            file = DependencyUtilities.downloadJitpackDependency(dependency, dir.getAbsolutePath());
          } catch (final IOException e) {
            Logger.info("Could NOT find " + artifact + " in Jitpack Central Repository!");
            e.printStackTrace();
          }
        }
        if (file != null) {
          files.add(file);
        }
      }
    }
  }

  /** Relocates Dependencies. */
  public void relocate() {
    for (final File f : dir.listFiles()) {
      if (f.getName().contains("asm")) {
        try {
          DependencyUtilities.loadDependency(f);
          files.remove(f);
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }
    final List<Relocation> relocations =
        Arrays.stream(JarRelocationConvention.values())
            .map(JarRelocationConvention::getRelocation)
            .collect(Collectors.toList());
    for (final File f : files) {
      final JarRelocator relocator =
          new JarRelocator(f, new File(relocatedDir, f.getName()), relocations);
      try {
        relocator.run();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /** Install and load. */
  public void load() {
    for (final File f : relocatedDir.listFiles()) {
      try {
        DependencyUtilities.loadDependency(f);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Check if dependency exists in the directory beforehand.
   *
   * @param dir the directory
   * @param dependency the dependency
   * @return the boolean
   */
  private boolean checkExists(
      @NotNull final File dir, @NotNull final RepositoryDependency dependency) {
    for (final File f : dir.listFiles()) {
      if (f.getName().contains(dependency.getArtifact())) {
        return true;
      }
    }
    return false;
  }
}
