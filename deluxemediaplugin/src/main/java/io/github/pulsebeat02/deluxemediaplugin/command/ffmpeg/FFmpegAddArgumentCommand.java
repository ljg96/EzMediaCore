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

public final class FFmpegAddArgumentCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final DeluxeMediaPlugin plugin;
  private final FFmpegCommandExecutor ffmpeg;

  public FFmpegAddArgumentCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final FFmpegCommandExecutor executor) {
    this.plugin = plugin;
    this.ffmpeg = executor;
    this.node =
        this.literal("add")
            .then(
                this.argument("arguments", StringArgumentType.greedyString())
                    .executes(this::addArgument))
            .then(
                this.argument("index", IntegerArgumentType.integer())
                    .then(
                        this.argument("arguments", StringArgumentType.greedyString())
                            .executes(this::addIndexArgument)))
            .build();
  }

  private int addIndexArgument(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String str = context.getArgument("arguments", String.class);
    final int index = context.getArgument("index", Integer.TYPE);
    this.addMultipleArguments(index, str.split(" "));
    audience.sendMessage(Locale.ADD_FFMPEG_ARG_INDX.build(str, index));
    return SINGLE_SUCCESS;
  }

  private void addMultipleArguments(final int index, final String @NotNull [] args) {
    for (final String arg : args) {
      if (arg.contains("=")) {
        this.handleSpecialArgument(index, arg);
      } else {
        this.handleNormalArgument(index, arg);
      }
    }
  }

  private void addMultipleArguments(final String @NotNull [] arguments) {
    for (final String argument : arguments) {
      if (argument.contains("=")) {
        this.handleSpecialArgument(argument);
      } else {
        this.handleNormalArgument(argument);
      }
    }
  }

  private void handleNormalArgument(final int index, @NotNull final String arg) {
    this.ffmpeg.addArgument(arg, index);
  }

  private void handleNormalArgument(@NotNull final String argument) {
    this.ffmpeg.addArgument(argument);
  }

  private void handleSpecialArgument(final int index, @NotNull final String arg) {
    final String[] split = arg.split("=");
    this.ffmpeg.addArguments(split[0], split[1], index);
  }

  private void handleSpecialArgument(@NotNull final String argument) {
    final String[] split = argument.split("=");
    this.ffmpeg.addArguments(split[0], split[1]);
  }

  private int addArgument(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String str = context.getArgument("arguments", String.class);
    final String[] arguments = str.split(" ");
    this.addMultipleArguments(arguments);
    audience.sendMessage(Locale.ADD_FFMPEG_ARG.build(str));
    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
