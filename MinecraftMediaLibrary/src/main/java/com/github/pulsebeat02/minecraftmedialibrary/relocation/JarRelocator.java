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

package com.github.pulsebeat02.minecraftmedialibrary.relocation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/** Relocates classes and resources within a jar file. */
public final class JarRelocator {

  /** The input jar */
  private final File input;
  /** The output jar */
  private final File output;
  /** The relocating remapper */
  private final RelocatingRemapper remapper;

  /** If the {@link #run()} method has been called yet */
  private final AtomicBoolean used = new AtomicBoolean(false);

  /**
   * Creates a new instance with the given settings.
   *
   * @param input the input jar file
   * @param output the output jar file
   * @param relocations the relocations
   */
  public JarRelocator(File input, File output, Collection<Relocation> relocations) {
    this.input = input;
    this.output = output;
    this.remapper = new RelocatingRemapper(relocations);
  }

  /**
   * Creates a new instance with the given settings.
   *
   * @param input the input jar file
   * @param output the output jar file
   * @param relocations the relocations
   */
  public JarRelocator(File input, File output, Map<String, String> relocations) {
    this.input = input;
    this.output = output;
    Collection<Relocation> c = new ArrayList<>(relocations.size());
    for (Map.Entry<String, String> entry : relocations.entrySet()) {
      c.add(new Relocation(entry.getKey(), entry.getValue()));
    }
    this.remapper = new RelocatingRemapper(c);
  }

  /**
   * Executes the relocation task
   *
   * @throws IOException if an exception is encountered whilst performing i/o with the input or
   *     output file
   */
  public void run() throws IOException {
    if (this.used.getAndSet(true)) {
      throw new IllegalStateException("#run has already been called on this instance");
    }

    try (JarOutputStream out =
        new JarOutputStream(new BufferedOutputStream(new FileOutputStream(this.output)))) {
      try (JarFile in = new JarFile(this.input)) {
        JarRelocatorTask task = new JarRelocatorTask(this.remapper, out, in);
        task.processEntries();
      }
    }
  }
}
