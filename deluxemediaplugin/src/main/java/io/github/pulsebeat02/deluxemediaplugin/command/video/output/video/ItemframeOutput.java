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
package io.github.pulsebeat02.deluxemediaplugin.command.video.output.video;

import static io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration.DELAY_0_MS;
import static io.github.pulsebeat02.ezmediacore.callback.Identifier.ofIdentifier;
import static io.github.pulsebeat02.ezmediacore.callback.Viewers.ofPlayers;
import static io.github.pulsebeat02.ezmediacore.dimension.Dimension.ofDimension;
import static io.github.pulsebeat02.ezmediacore.player.SoundKey.ofSound;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import io.github.pulsebeat02.ezmediacore.callback.CallbackBuilder;
import io.github.pulsebeat02.ezmediacore.callback.MapCallback;
import io.github.pulsebeat02.ezmediacore.callback.MapCallback.Builder;
import io.github.pulsebeat02.ezmediacore.player.VideoBuilder;
import java.util.Collection;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ItemframeOutput extends VideoOutput {

  public ItemframeOutput() {
    super("ITEMFRAME");
  }

  @Override
  public boolean createVideoPlayer(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final VideoCommandAttributes attributes,
      @NotNull final CommandSender sender,
      @NotNull final Collection<? extends Player> players) {
    attributes.setPlayer(this.createVideoBuilder(plugin, attributes, players).build());
    return true;
  }

  @NotNull
  private VideoBuilder createVideoBuilder(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final VideoCommandAttributes attributes,
      @NotNull final Collection<? extends Player> players) {

    final MapCallback.Builder builder = this.createMapBuilder(attributes, players);

    final VideoBuilder videoBuilder = VideoBuilder.unspecified();
    videoBuilder.dims(ofDimension(attributes.getPixelWidth(), attributes.getPixelHeight()));
    videoBuilder.soundKey(ofSound("emc"));
    videoBuilder.callback(builder.build(plugin.library()));

    return videoBuilder;
  }

  @NotNull
  private MapCallback.Builder createMapBuilder(
      @NotNull final VideoCommandAttributes attributes,
      @NotNull final Collection<? extends Player> players) {

    final Builder builder = CallbackBuilder.map();
    builder.algorithm(attributes.getDitherType().getAlgorithm());
    builder.blockWidth(attributes.getPixelWidth());
    builder.map(ofIdentifier(0));
    builder.dims(ofDimension(attributes.getFrameWidth(), attributes.getFrameHeight()));
    builder.viewers(ofPlayers(players));
    builder.delay(DELAY_0_MS);

    return builder;
  }
}
