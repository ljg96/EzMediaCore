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

package io.github.pulsebeat02.minecraftmedialibrary.frame.highlight;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.frame.JaffreeVideoPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A VLCJ integrated player used to play videos in Minecraft. The library uses a callback for the
 * specific function from native libraries. It renders it in debug highlights. Linux compatible.
 */
public class LinuxBlockHighlightPlayer extends JaffreeVideoPlayer {

  /**
   * Instantiates a new LinuxBlockHighlightPlayer.
   *
   * @param library the library
   * @param url the url
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public LinuxBlockHighlightPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String url,
      @NotNull final BlockHighlightCallbackPrototype callback,
      final int width,
      final int height) {
    super(library, "Debug Highlights", url, width, height, callback);
  }

  /**
   * Returns a new builder class to use.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** The type Builder. */
  public static class Builder {

    private BlockHighlightCallbackPrototype callback;
    private int width = 10;
    private int height = 10;
    private String url;

    private Builder() {}

    public Builder width(final int width) {
      this.width = width;
      return this;
    }

    public Builder height(final int height) {
      this.height = height;
      return this;
    }

    public Builder callback(final BlockHighlightCallbackPrototype callback) {
      this.callback = callback;
      return this;
    }

    public Builder url(final String url) {
      this.url = url;
      return this;
    }

    public LinuxBlockHighlightPlayer build(@NotNull final MediaLibrary library) {
      return new LinuxBlockHighlightPlayer(library, url, callback, width, height);
    }
  }
}
