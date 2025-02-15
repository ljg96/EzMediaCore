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

package io.github.pulsebeat02.deluxemediaplugin.command.ffmpeg;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegCommandExecutor;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class FFmpegRemoveArgumentCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final DeluxeMediaPlugin plugin;
  private final FFmpegCommandExecutor ffmpeg;

  public FFmpegRemoveArgumentCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final FFmpegCommandExecutor executor) {
    this.plugin = plugin;
    this.ffmpeg = executor;
    this.node =
        this.literal("remove")
            .then(
                this.argument("argument", StringArgumentType.word()).executes(this::removeArgument))
            .then(
                this.argument("index", IntegerArgumentType.integer())
                    .executes(this::removeIndexArgument))
            .build();
  }

  private int removeIndexArgument(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.plugin.audience().sender(context.getSource());
    final int index = context.getArgument("index", Integer.TYPE);
    final String arg = this.ffmpeg.getArguments().get(index);
    this.removeArgument(index);
    audience.sendMessage(Locale.REMOVE_FFMPEG_ARG_INDX.build(arg, index));
    return SINGLE_SUCCESS;
  }

  private void removeArgument(final int index) {
    this.ffmpeg.removeArgument(index);
  }

  private void removeArgument(@NotNull final String argument) {
    this.ffmpeg.removeArgument(argument);
  }

  private int removeArgument(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String argument = context.getArgument("argument", String.class);
    this.removeArgument(argument);
    audience.sendMessage(Locale.REMOVE_FFMPEG_ARG.build(argument));
    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
