/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.player;

import com.google.common.annotations.Beta;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Unfortunately, due to JNA issues, this class must be mutable and there can only be one instance.
 */

@Beta
public abstract class MediaPlayer implements VideoPlayer {

  private final MediaLibraryCore core;
  private final Dimension dimensions;

  private final FrameConfiguration fps;
  private Viewers viewers;

  private Callback callback;
  private final SoundKey key;

  private MrlConfiguration directVideo;
  private MrlConfiguration directAudio;
  private Consumer<MrlConfiguration> playAudio;
  private Runnable stopAudio;

  private PlayerControls controls;

  MediaPlayer(
      @NotNull final Callback callback,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension,
      @NotNull final FrameConfiguration fps,
      @Nullable final SoundKey key) {
    this.core = callback.getCore();
    this.callback = callback;
    this.dimensions = pixelDimension;
    this.key =
        key == null
            ? SoundKey.ofSound(callback.getCore().getPlugin().getName().toLowerCase(Locale.ROOT))
            : key;
    this.fps = fps;
    this.viewers = viewers;
    this.playAudio = this.getPlayAudioRunnable();
    this.stopAudio = this.getStopAudioRunnable();
  }

  private @NotNull Runnable getStopAudioRunnable() {
    return () -> {
      for (final Player player : this.viewers.getPlayers()) {
        player.stopSound(this.key.getName(), SoundCategory.MASTER);
      }
    };
  }

  private @NotNull Consumer<MrlConfiguration> getPlayAudioRunnable() {
    return (mrl) ->
        this.viewers
            .getPlayers()
            .forEach(
                player ->
                    player.playSound(
                        player.getLocation(),
                        this.key.getName(),
                        SoundCategory.MASTER,
                        100.0F,
                        1.0F));
  }

  @Override
  public @NotNull Callback getCallback() {
    return this.callback;
  }

  @Override
  public void setCallback(@NotNull final Callback callback) {
    this.callback = callback;
  }

  @Override
  public @NotNull SoundKey getSoundKey() {
    return this.key;
  }

  @Override
  public @NotNull PlayerControls getPlayerState() {
    return this.controls;
  }

  @Override
  public void start(@NotNull final MrlConfiguration mrl, @NotNull final Object... arguments) {
    this.controls = PlayerControls.START;
    this.onPlayerStateChange(mrl, this.controls, arguments);
  }

  @Override
  public void pause() {
    this.controls = PlayerControls.PAUSE;
    this.onPlayerStateChange(null, this.controls);
  }

  @Override
  public void resume(@NotNull final MrlConfiguration mrl, @NotNull final Object... arguments) {
    this.controls = PlayerControls.RESUME;
    this.onPlayerStateChange(mrl, this.controls, arguments);
  }

  @Override
  public void release() {
    this.controls = PlayerControls.RELEASE;
    this.onPlayerStateChange(null, this.controls);
  }

  @Override
  public void onPlayerStateChange(
      @Nullable final MrlConfiguration mrl,
      @NotNull final PlayerControls controls,
      @NotNull final Object... arguments) {}

  @Override
  public void playAudio() {
    CompletableFuture.runAsync(() -> this.playAudio.accept(this.getDirectAudioMrl()));
  }

  @Override
  public void stopAudio() {
    CompletableFuture.runAsync(this.stopAudio);
  }

  @Override
  public @NotNull FrameConfiguration getFrameConfiguration() {
    return this.fps;
  }

  @Override
  public @NotNull Dimension getDimensions() {
    return this.dimensions;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public @NotNull Viewers getWatchers() {
    return this.viewers;
  }

  @Override
  public void setCustomAudioPlayback(@NotNull final Consumer<MrlConfiguration> runnable) {
    this.playAudio = runnable;
  }

  @Override
  public void setCustomAudioStopper(@NotNull final Runnable runnable) {
    this.stopAudio = runnable;
  }

  @Override
  public void setViewers(@NotNull final Viewers viewers) {
    this.viewers = viewers;
  }

  @Override
  public @NotNull MrlConfiguration getDirectVideoMrl() {
    return this.directVideo;
  }

  @Override
  public void setDirectVideoMrl(@NotNull final MrlConfiguration videoMrl) {
    this.directVideo = videoMrl;
  }

  @Override
  public @NotNull MrlConfiguration getDirectAudioMrl() {
    return this.directAudio;
  }

  @Override
  public void setDirectAudioMrl(@NotNull final MrlConfiguration audioMrl) {
    this.directAudio = audioMrl;
  }
}
