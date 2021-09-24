package io.github.pulsebeat02.deluxemediaplugin.discord.command;

import io.github.pulsebeat02.deluxemediaplugin.discord.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.discord.audio.MusicManager;
import java.util.Set;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayAudioCommand extends DiscordBaseCommand {

  public PlayAudioCommand(@NotNull final MediaBot bot) {
    super(bot, "play", Set.of());
  }

  @Override
  public boolean execute(@NotNull final Message executor, final String @Nullable [] arguments) {
    if (arguments == null || arguments.length < 1) {
      executor
          .getChannel()
          .sendMessageEmbeds(
              new EmbedBuilder()
                  .setTitle("User Error")
                  .setDescription("Invalid arguments! Specify a media source argument to play.")
                  .build())
          .queue();
      return false;
    }
    final MusicManager manager = this.getBot().getMusicManager();
    manager.joinVoiceChannel();
    manager.addTrack(executor.getChannel(), arguments[0]);
    executor
        .getChannel()
        .sendMessageEmbeds(
            new EmbedBuilder()
                .setTitle("Audio Voice Channel Connection")
                .setDescription("Connected to voice channel!")
                .build())
        .queue();
    return true;
  }
}
