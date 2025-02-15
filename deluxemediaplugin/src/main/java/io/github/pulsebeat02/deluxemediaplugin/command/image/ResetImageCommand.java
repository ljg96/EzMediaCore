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

package io.github.pulsebeat02.deluxemediaplugin.command.image;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleEmptyOptional;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.requiresPlayer;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.config.PersistentPictureManager;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.ezmediacore.image.Image;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public final class ResetImageCommand implements CommandSegment.Literal<CommandSender>, Listener {

  private final LiteralCommandNode<CommandSender> node;
  private final ImageCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;

  public ResetImageCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ImageCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    this.node =
        this.literal("purge")
            .then(
                this.literal("map")
                    .then(
                        this.argument("id", IntegerArgumentType.integer())
                            .executes(this::purgeMap)))
            .then(this.literal("all").executes(this::purgeAllMaps))
            .build();
    plugin
        .getBootstrap()
        .getServer()
        .getPluginManager()
        .registerEvents(this, plugin.getBootstrap());
  }

  private int purgeMap(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final PersistentPictureManager manager = this.plugin.getPictureManager();
    final int id = context.getArgument("id", Integer.TYPE);
    final Optional<Image> image =
        manager.getImages().stream().filter(img -> img.getMaps().contains(id)).findAny();
    if (handleEmptyOptional(audience, Locale.ERR_IMAGE_NOT_LOADED.build(), image)) {
      return SINGLE_SUCCESS;
    }

    this.resetMaps(image.get());

    audience.sendMessage(Locale.PURGE_MAP.build(id));

    return SINGLE_SUCCESS;
  }

  private void resetMaps(@NotNull final Image image) {
    image.resetMaps();
  }

  private int purgeAllMaps(@NotNull final CommandContext<CommandSender> context) {

    final CommandSender sender = context.getSource();
    final Audience audience = this.plugin.audience().sender(sender);

    if (requiresPlayer(this.plugin, sender)) {
      return SINGLE_SUCCESS;
    }

    this.addWarningListener((Player) sender);

    audience.sendMessage(Locale.PURGE_ALL_MAPS_VERIFY.build());

    return SINGLE_SUCCESS;
  }

  private void addWarningListener(@NotNull final Player sender) {
    this.attributes.getListen().add(sender.getUniqueId());
  }

  @EventHandler
  public void onPlayerChat(@NotNull final AsyncPlayerChatEvent event) {

    final Player p = event.getPlayer();
    final UUID uuid = p.getUniqueId();
    final Set<UUID> listen = this.attributes.getListen();

    if (listen.contains(uuid)) {
      event.setCancelled(true);

      final Audience audience = this.plugin.audience().player(p);
      final String message = event.getMessage();
      this.handleMessage(audience, message);

      listen.remove(uuid);
    }
  }

  private void handleMessage(@NotNull final Audience audience, @NotNull final String message) {
    if (message.equals("YES")) {
      this.clearImages();
      audience.sendMessage(Locale.PURGED_ALL_MAPS.build());
    } else {
      audience.sendMessage(Locale.CANCELLED_PURGE_ALL_MAPS.build());
    }
  }

  private void clearImages() {
    final List<Image> images = this.plugin.getPictureManager().getImages();
    images.forEach(Image::resetMaps);
    images.clear();
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
