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
package io.github.pulsebeat02.ezmediacore.ffmpeg;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FFmpegAudioExtractor extends FFmpegCommandExecutor implements AudioExtractor {

  private final String input;
  private final String output;

  FFmpegAudioExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String input,
      @NotNull final String output) {
    super(core);
    this.input = input;
    this.output = output;
    this.clearArguments();
    this.addMultipleArguments(this.generateArguments(configuration));
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull FFmpegAudioExtractor ofFFmpegAudioExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String input,
      @NotNull final String output) {
    return new FFmpegAudioExtractor(core, configuration, input, output);
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull FFmpegAudioExtractor ofFFmpegAudioExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final Path input,
      @NotNull final Path output) {
    return ofFFmpegAudioExtractor(core, configuration, input.toString(), output.toString());
  }

  @Contract("_ -> new")
  private @NotNull List<String> generateArguments(@NotNull final AudioConfiguration configuration) {
    return new ArrayList<>(
        List.of(
            this.getCore().getFFmpegPath().toString(),
            "-i",
            this.input,
            "-vn",
            "-acodec",
            "libvorbis",
            "-ab",
            String.valueOf(configuration.getBitrate()),
            "-ac",
            String.valueOf(configuration.getChannels()),
            "-ar",
            String.valueOf(configuration.getSamplingRate()),
            "-vol",
            String.valueOf(configuration.getVolume()),
            "-ss",
            String.valueOf(configuration.getStartTime()),
            "-y"));
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) {
    this.addArgument(this.output);
    super.executeWithLogging(logger);
  }

  @Override
  public @NotNull String getInput() {
    return this.input;
  }

  @Override
  public @NotNull String getOutput() {
    return this.output;
  }
}
