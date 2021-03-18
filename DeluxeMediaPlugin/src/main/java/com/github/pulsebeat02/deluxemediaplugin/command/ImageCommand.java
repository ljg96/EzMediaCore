/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/28/2021
 * ============================================================================
 */

package com.github.pulsebeat02.deluxemediaplugin.command;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.image.MapImage;
import com.github.pulsebeat02.minecraftmedialibrary.utility.FileUtilities;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;

public class ImageCommand extends BaseCommand implements Listener {

  private final Set<MapImage> images;
  private final Set<UUID> listen;
  private final LiteralCommandNode<CommandSender> literalNode;
  private int width;
  private int height;

  public ImageCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "dither", executor, "deluxemediaplugin.command.image", "");
    Bukkit.getPluginManager().registerEvents(this, plugin);
    listen = new HashSet<>();
    images = new HashSet<>();
    width = 1;
    height = 1;
    final LiteralArgumentBuilder<CommandSender> builder = literal(getName());
    builder
        .requires(super::testPermission)
        .then(
            literal("reset")
                .then(
                    literal("map")
                        .then(argument("id", StringArgumentType.word()).executes(this::resetMap)))
                .then(literal("all").executes(this::resetAllMaps)))
        .then(
            literal("set")
                .then(
                    literal("map")
                        .then(
                            argument("id", LongArgumentType.longArg())
                                .then(
                                    argument("mrl", StringArgumentType.word())
                                        .executes(this::setImage))))
                .then(
                    literal("dimensions")
                        .then(
                            argument("dim", StringArgumentType.string())
                                .executes(this::setDimensions))))
        .then(literal("rickroll").executes(this::setRickRoll));
    literalNode = builder.build();
  }

  private boolean isUrl(@NotNull final String mrl) {
    return (mrl.startsWith("http://")) || mrl.startsWith("https://") && mrl.endsWith(".png");
  }

  @Override
  public LiteralCommandNode<CommandSender> getCommandNode() {
    return literalNode;
  }

  @Override
  public String usage() {
    return ChatColor.RED + "/image, /image reset all, /image reset map [Map ID], /image rickroll";
  }

  private int setRickRoll(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final MinecraftMediaLibrary library = getPlugin().getLibrary();
    final File f =
        FileUtilities.downloadImageFile(
            "https://images.news18.com/ibnlive/uploads/2020/12/1607660925_untitled-design-2020-12-11t095722.206.png",
            library.getPlugin().getDataFolder().getAbsolutePath());
    new MapImage(library, 69, f, width, height).drawImage();
    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Gottem"));
    return 1;
  }

  private int setDimensions(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final Optional<int[]> opt =
        ChatUtilities.checkDimensionBoundaries(sender, context.getArgument("dims", String.class));
    if (!opt.isPresent()) {
      return 1;
    }
    final int[] dims = opt.get();
    width = dims[0];
    height = dims[1];
    sender.sendMessage(
        ChatUtilities.formatMessage(
            ChatColor.GOLD
                + "Changed item frame dimensions to "
                + width
                + ":"
                + height
                + " (width:height"));
    return 1;
  }

  private int setImage(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final OptionalLong opt =
        ChatUtilities.checkMapBoundaries(sender, context.getArgument("id", String.class));
    if (!opt.isPresent()) {
      return 1;
    }
    final int id = (int) opt.getAsLong();
    final String mrl = context.getArgument("mrl", String.class);
    final String successful =
        ChatUtilities.formatMessage(ChatColor.GOLD + "Successfully drew image on map " + id);
    final DeluxeMediaPlugin plugin = getPlugin();
    final MinecraftMediaLibrary library = plugin.getLibrary();
    if (isUrl(mrl)) {
      final File img =
          FileUtilities.downloadImageFile(mrl, plugin.getDataFolder().getAbsolutePath());
      new MapImage(library, id, img, width, height).drawImage();
      sender.sendMessage(successful);
    } else {
      final File img = new File(plugin.getDataFolder(), mrl);
      if (img.exists()) {
        new MapImage(library, id, img, width, height).drawImage();
        sender.sendMessage(successful);
      } else {
        sender.sendMessage(
            ChatUtilities.formatMessage(
                ChatColor.RED + "File " + img.getName() + " cannot be found!"));
      }
    }
    return 1;
  }

  private int resetMap(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final OptionalLong opt =
        ChatUtilities.checkMapBoundaries(sender, context.getArgument("id", String.class));
    if (!opt.isPresent()) {
      return 1;
    }
    final int id = (int) opt.getAsLong();
    final Iterator<MapImage> itr = images.iterator();
    while (itr.hasNext()) {
      if (itr.next().getMap() == id) {
        itr.remove();
        break;
      }
    }
    MapImage.resetMap(getPlugin().getLibrary(), id);
    sender.sendMessage(
        ChatUtilities.formatMessage(ChatColor.GOLD + "Successfully purged the map with ID " + id));
    return 1;
  }

  private int resetAllMaps(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    listen.add(((Player) sender).getUniqueId());
    sender.sendMessage(
        ChatUtilities.formatMessage(
            ChatColor.RED
                + "Are you sure you want to purge and delete all maps? Type YES if you would like to continue."));
    return 1;
  }

  @EventHandler
  public void onPlayerChat(final AsyncPlayerChatEvent event) {
    final Player p = event.getPlayer();
    if (listen.contains(p.getUniqueId())) {
      if (event.getMessage().equalsIgnoreCase("YES")) {
        for (final MapImage image : images) {
          MapImage.resetMap(getPlugin().getLibrary(), image.getMap());
        }
        images.clear();
        p.sendMessage(
            ChatUtilities.formatMessage(ChatColor.GOLD + "Successfully purged all image maps"));
      } else {
        p.sendMessage(
            ChatUtilities.formatMessage(ChatColor.GOLD + "Cancelled purge of all image maps"));
      }
    }
  }
}
