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

package com.github.pulsebeat02.minecraftmedialibrary.concurrent;

import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.AbstractPackHolder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AsyncResourcepackBuilder {

  private final AbstractPackHolder packHolder;

  /**
   * Instantiates a new AsyncResourcepackBuilder.
   *
   * @param packHolder the pack holder
   */
  public AsyncResourcepackBuilder(@NotNull final AbstractPackHolder packHolder) {
    this.packHolder = packHolder;
  }

  /**
   * Build a resource pack using CompletableFuture.
   *
   * @return the CompletableFuture
   */
  public CompletableFuture<Void> buildResourcePack() {
    return CompletableFuture.runAsync(packHolder::buildResourcePack);
  }
}
