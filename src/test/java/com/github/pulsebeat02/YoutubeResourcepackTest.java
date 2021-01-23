package com.github.pulsebeat02;

import com.github.pulsebeat02.extractor.YoutubeExtraction;
import com.github.pulsebeat02.image.ImageMap;
import com.github.pulsebeat02.resourcepack.ResourcepackWrapper;
import com.github.pulsebeat02.resourcepack.hosting.HttpDaemonProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class YoutubeResourcepackTest extends JavaPlugin {

    private MinecraftMediaLibrary library;

    @Override
    public void onEnable() {
        library = new MinecraftMediaLibrary(this, getDataFolder().getPath(), true);
    }

    public String getResourcepackUrlYoutube(final String youtubeUrl, final String directory, final int port) {

        YoutubeExtraction extraction = new YoutubeExtraction(youtubeUrl, directory);
        extraction.downloadVideo();
        extraction.extractAudio();

        ResourcepackWrapper wrapper = new ResourcepackWrapper.Builder()
                .setAudio(extraction.getAudio())
                .setDescription("Youtube Video: " + extraction.getVideoTitle())
                .setPath(directory)
                .setPackFormat(6)
                .createResourcepackHostingProvider(library);
        wrapper.buildResourcePack();

        HttpDaemonProvider hosting = new HttpDaemonProvider(directory, port);
        hosting.startServer();

        return hosting.generateUrl(Paths.get(directory));

    }

    public void displayImage(final int map, final File image) throws IOException {

        BufferedImage bi = ImageIO.read(image);

        ImageMap imageMap = new ImageMap.Builder()
                .setMap(map)
                .setViewers(getOnlinePlayerUUIDs())
                .setWidth(bi.getWidth())
                .setHeight(bi.getHeight())
                .createImageMap(library);

        imageMap.drawImage();

    }

    public UUID[] getOnlinePlayerUUIDs() {
        List<? extends Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        UUID[] uuids = new UUID[players.size()];
        for (int i = 0; i < players.size(); i++) {
            uuids[i] = players.get(i).getUniqueId();
        }
        return uuids;
    }

}
