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

package io.github.pulsebeat02.deluxemediaplugin;

import io.github.pulsebeat02.deluxemediaplugin.bot.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandHandler;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import io.github.pulsebeat02.deluxemediaplugin.config.BotConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.EncoderConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.HttpAudioConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.HttpConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.PersistentPictureManager;
import io.github.pulsebeat02.deluxemediaplugin.config.ServerInfo;
import io.github.pulsebeat02.deluxemediaplugin.executors.FixedExecutors;
import io.github.pulsebeat02.deluxemediaplugin.json.MediaAttributesData;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.deluxemediaplugin.update.UpdateChecker;
import io.github.pulsebeat02.deluxemediaplugin.utility.component.CommandUtils;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import io.github.pulsebeat02.ezmediacore.LibraryProvider;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.ffmpeg.EnhancedExecution;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DeluxeMediaPlugin {

  private final JavaPlugin plugin;
  private BukkitAudiences audiences;
  private Audience console;
  private AudioConfiguration audioConfiguration;
  private MediaLibraryCore library;
  private CommandHandler handler;
  private PersistentPictureManager manager;
  private HttpServer server;
  private MediaBot mediaBot;
  private ServerInfo httpAudioServer;
  private VideoCommandAttributes attributes;
  private MediaAttributesData mediaAttributesData;

  DeluxeMediaPlugin(@NotNull final JavaPlugin plugin) {
    this.plugin = plugin;
  }

  void enable() {
    this.registerAudiences();
    this.startLibrary();
    this.loadPersistentData();
    this.registerCommands();
    this.startMetrics();
    this.finishEnabling();
  }

  void disable() {
    this.shutdownLibrary();
    this.deserializeData();
    this.unregisterCommands();
    this.disableBot();
    this.cancelNativeTasks();
    this.finishDisabling();
  }

  void load() {
    this.finishLoading();
  }

  private void registerAudiences() {
    this.audiences = BukkitAudiences.create(this.plugin);
    this.console = this.audiences.console();
  }

  private void deserializeData() {
    Nill.ifNot(
        this.mediaAttributesData, () -> this.mediaAttributesData.deserialize(this.attributes));
    this.console.sendMessage(Locale.DESERIALIZE_DATA.build());
  }

  private void startMetrics() {
    new Metrics(this.plugin, 10229);
    this.console.sendMessage(Locale.FIN_METRICS_INIT.build());
  }

  private void finishLoading() {}

  private void finishEnabling() {
    this.checkUpdates();
    this.console.sendMessage(Locale.FIN_PLUGIN_INIT.build());
    this.console.sendMessage(Locale.WELCOME.build());
  }

  private void finishDisabling() {
    this.console.sendMessage(Locale.GOODBYE.build());
  }

  private void startLibrary() {
    this.console.sendMessage(Locale.PLUGIN_LOGO.build());
    this.console.sendMessage(Locale.ENABLE_PLUGIN.build());
    this.console.sendMessage(Locale.EMC_INIT.build());
    this.library = LibraryProvider.builder().plugin(this.plugin).build();
    this.library.initialize();
    this.console.sendMessage(Locale.FIN_EMC_INIT.build());
  }

  private void checkUpdates() {
    new UpdateChecker(this).check();
  }

  private void disableBot() {
    Nill.ifNot(this.mediaBot, () -> this.mediaBot.getJDA().shutdown());
    this.console.sendMessage(Locale.DISABLE_BOT.build());
  }

  private void unregisterCommands() {
    Nill.ifNot(
        this.handler,
        () -> this.handler.getCommands().forEach(cmd -> CommandUtils.unregisterCommand(this, cmd)));
    this.console.sendMessage(Locale.DISABLE_COMMANDS.build());
  }

  private void shutdownLibrary() {
    this.console.sendMessage(Locale.DISABLE_PLUGIN.build());
    if (this.library != null) {
      this.library.shutdown();
      this.console.sendMessage(Locale.DISABLE_EMC.build());
    } else {
      this.console.sendMessage(Locale.ERR_EMC_SHUTDOWN.build());
    }
  }

  private void cancelNativeTasks() {
    Nill.ifNot(this.attributes, this::cancelExternalTasks);
    this.console.sendMessage(Locale.CANCELLED_TASKS.build());
  }

  private void cancelExternalTasks() {
    try {
      this.cancelNativeExtractor();
      this.cancelNativeStreamExtractor();
    } catch (final Exception e) {
      throw new AssertionError(e);
    }
  }

  private void cancelNativeExtractor() {
    final EnhancedExecution extractor = this.attributes.getExtractor();
    Nill.ifNot(extractor, () -> Try.closeable(extractor));
  }

  private void cancelNativeStreamExtractor() {
    final EnhancedExecution streamExtractor = this.attributes.getStreamExtractor();
    Nill.ifNot(streamExtractor, () -> Try.closeable(streamExtractor));
  }

  private void createFolders() {
    final Path folder = this.plugin.getDataFolder().toPath();
    Set.of(folder.resolve("configuration"), folder.resolve("data"))
        .forEach(FileUtils::createFolderIfNotExistsExceptionally);
  }

  private void readConfigurationFiles() throws IOException {
    this.readHttpConfiguration();
    this.readEncoderConfiguration();
    this.readBotConfiguration();
    this.readStreamAudioConfiguration();
    this.readPictureData();
  }

  private void readHttpConfiguration() throws IOException {
    final HttpConfiguration httpConfiguration = new HttpConfiguration(this);
    httpConfiguration.read();
    this.server = httpConfiguration.serialize();
  }

  private void readEncoderConfiguration() throws IOException {
    final EncoderConfiguration encoderConfiguration = new EncoderConfiguration(this);
    encoderConfiguration.read();
    this.audioConfiguration = encoderConfiguration.serialize();
  }

  private void readBotConfiguration() throws IOException {
    final BotConfiguration botConfiguration = new BotConfiguration(this);
    botConfiguration.read();
    this.mediaBot = botConfiguration.serialize();
  }

  private void readStreamAudioConfiguration() throws IOException {
    final HttpAudioConfiguration audioConfiguration = new HttpAudioConfiguration(this);
    audioConfiguration.read();
    this.httpAudioServer = audioConfiguration.serialize();
  }

  private void readJsonFiles() throws IOException {
    this.mediaAttributesData = new MediaAttributesData(this);
    this.attributes = this.mediaAttributesData.serialize();
  }

  private void readPictureData() throws IOException {
    this.manager = new PersistentPictureManager(this);
    this.manager.startTask();
  }

  private void loadPersistentData() {
    try {
      this.createFolders();
      this.readConfigurationFiles();
      this.readJsonFiles();
      this.writeToFile();
    } catch (final IOException e) {
      this.console.sendMessage(Locale.ERR_PERSISTENT_INIT.build());
      throw new AssertionError(e);
    }
    this.console.sendMessage(Locale.FIN_PERSISTENT_INIT.build());
  }

  private void writeToFile() {
    FixedExecutors.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(
        this::deserializeAttributes, 0L, 5L, TimeUnit.MINUTES);
  }

  private void deserializeAttributes() {
    this.mediaAttributesData.deserialize(this.attributes);
  }

  private void registerCommands() {
    this.handler = new CommandHandler(this);
    this.console.sendMessage(Locale.FIN_COMMANDS_INIT.build());
  }

  public Audience getConsoleAudience() {
    return this.console;
  }

  public @NotNull JavaPlugin getBootstrap() {
    return this.plugin;
  }

  public @NotNull PersistentPictureManager getPictureManager() {
    return this.manager;
  }

  public @NotNull AudioConfiguration getAudioConfiguration() {
    return this.audioConfiguration;
  }

  public @NotNull HttpServer getHttpServer() {
    return this.server;
  }

  public @Nullable MediaBot getMediaBot() {
    return this.mediaBot;
  }

  public @NotNull MediaLibraryCore library() {
    return this.library;
  }

  public @NotNull BukkitAudiences audience() {
    return this.audiences;
  }

  public @NotNull VideoCommandAttributes getAttributes() {
    return this.attributes;
  }

  public @Nullable ServerInfo getHttpAudioServer() {
    return this.httpAudioServer;
  }
}
