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
/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.deluxemediaplugin.command.audio;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.gold;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.red;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.ffmpeg.YoutubeVideoAudioExtractor;
import io.github.pulsebeat02.ezmediacore.resourcepack.ResourcepackSoundWrapper;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResourcepackUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class AudioLoadCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final AudioCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;

  public AudioLoadCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final AudioCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    this.node =
        this.literal("load")
            .then(this.argument("mrl", StringArgumentType.greedyString()).executes(this::loadAudio))
            .then(this.literal("resourcepack").executes(this::sendResourcepack))
            .build();
  }

  private void loadSoundMrl(@NotNull final String mrl) {

    final MediaLibraryCore core = this.plugin.library();
    final Path audio = core.getAudioPath().resolve("audio.ogg");

    try {
      new YoutubeVideoAudioExtractor(core, this.plugin.getAudioConfiguration(), mrl, audio)
          .executeAsync();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    this.attributes.setAudio(audio);
  }

  private boolean loadSoundFile(@NotNull final String mrl, @NotNull final Audience audience) {
    final Path file = Path.of(mrl);
    if (Files.exists(file)) {
      this.attributes.setAudio(file);
    } else {
      red(audience, "The mrl specified is not valid! (Must be Youtube Link or Audio File)");
      return true;
    }
    return false;
  }

  private void wrapResourcepack() {

    this.attributes.setCompletion(false);

    try {

      final HttpServer daemon = this.plugin.getHttpServer();
      final ResourcepackSoundWrapper wrapper =
          new ResourcepackSoundWrapper(
              daemon.getDaemon().getServerPath().resolve("resourcepack.zip"), "Audio Pack", 6);
      wrapper.addSound(this.plugin.getName().toLowerCase(Locale.ROOT), this.attributes.getAudio());
      wrapper.wrap();

      final Path path = wrapper.getResourcepackFilePath();
      this.attributes.setLink(daemon.createUrl(path));
      this.attributes.setHash(HashingUtils.createHashSHA(path).orElseThrow(AssertionError::new));

    } catch (final IOException e) {
      this.plugin.getLogger().severe("Failed to wrap resourcepack!");
      e.printStackTrace();
    }

    this.attributes.setCompletion(true);
  }

  private int loadAudio(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    final AtomicBoolean completion = this.attributes.getCompletion();

    if (this.isLoadingSound(audience)) {
      return SINGLE_SUCCESS;
    }

    gold(
        audience,
        "Creating a resourcepack for audio. Depending on the length of the video, it make take some time.");

    completion.set(false);

    if (mrl.startsWith("http")) {
      CompletableFuture.runAsync(() -> this.loadSoundMrl(mrl));
    } else if (this.loadSoundFile(mrl, audience)) {
      return SINGLE_SUCCESS;
    }

    CompletableFuture.runAsync(this::wrapResourcepack)
        .thenRunAsync(() -> this.forceResourcepackLoad(audience))
        .whenComplete((result, exception) -> completion.set(true));

    return SINGLE_SUCCESS;
  }

  private void forceResourcepackLoad(@NotNull final Audience audience) {
    final String url = this.attributes.getLink();
    final byte[] hash = this.attributes.getHash();
    ResourcepackUtils.forceResourcepackLoad(this.plugin.library(), url, hash);
    gold(
        audience,
        "Loaded Resourcepack Successfully! (URL: %s, Hash: %s)".formatted(url, new String(hash)));
  }

  private int sendResourcepack(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());

    if (this.attributes.getLink() == null && this.attributes.getHash() == null) {
      red(audience, "Please load a resourcepack first!");
      return SINGLE_SUCCESS;
    }

    final String url = this.attributes.getLink();
    final byte[] hash = this.attributes.getHash();

    ResourcepackUtils.forceResourcepackLoad(this.plugin.library(), url, hash);

    gold(audience, "Sent Resourcepack! (URL: %s, Hash: %s)".formatted(url, new String(hash)));

    return SINGLE_SUCCESS;
  }

  private boolean isLoadingSound(@NotNull final Audience audience) {
    if (!this.attributes.getCompletion().get()) {
      red(
          audience,
          "Please wait for the previous audio to extract first before loading another one!");
      return true;
    }
    return false;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
