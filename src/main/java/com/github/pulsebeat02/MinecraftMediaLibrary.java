package com.github.pulsebeat02;

import com.github.pulsebeat02.extractor.YoutubeExtraction;
import com.github.pulsebeat02.listener.PlayerJoinLeaveHandler;
import com.github.pulsebeat02.nms.PacketHandler;
import com.github.pulsebeat02.resourcepack.ResourcepackWrapper;
import com.github.pulsebeat02.resourcepack.hosting.HttpDaemonProvider;
import com.github.pulsebeat02.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

import java.nio.file.Paths;

public class MinecraftMediaLibrary {

    private final Plugin plugin;
    private final TinyProtocol protocol;
    private final String parent;
    private PacketHandler handler;
    private boolean vlcj;

    public MinecraftMediaLibrary(final Plugin plugin, final String path, final boolean isUsingVLCJ) {
        this.plugin = plugin;
        this.protocol = new TinyProtocol(plugin) {
            @Override
            public Object onPacketOutAsync(Player player, Channel channel, Object packet) {
                return handler.onPacketInterceptOut(player, packet);
            }

            @Override
            public Object onPacketInAsync(Player player, Channel channel, Object packet) {
                return handler.onPacketInterceptIn(player, packet);
            }
        };
        this.parent = path;
        this.vlcj = isUsingVLCJ;
        if (isUsingVLCJ) {
            new MediaPlayerFactory();
        }
        Bukkit.getPluginManager().registerEvents(new PlayerJoinLeaveHandler(this), plugin);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public PacketHandler getHandler() {
        return handler;
    }

    public TinyProtocol getProtocol() {
        return protocol;
    }

    public String getPath() {
        return parent;
    }

    public boolean isUsingVLCJ() {
        return vlcj;
    }

}
