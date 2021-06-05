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

package io.github.pulsebeat02.minecraftmedialibrary.frame.map;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.frame.VLCVideoPlayer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * A VLCJ integrated player used to play videos in Minecraft. The library uses a callback for the
 * specific function from native libraries. It renders it on maps.
 */
public class MapPlayer extends VLCVideoPlayer {

  /**
   * Instantiates a new MapPlayer.
   *
   * @param library the library
   * @param url the url
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public MapPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String url,
      @NotNull final MapDataCallbackPrototype callback,
      final int width,
      final int height) {
    super(library, "Itemframe Maps", url, width, height, callback);
  }

  /**
   * Instantiates a new MapPlayer.
   *
   * @param library the library
   * @param file the file
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public MapPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final Path file,
      @NotNull final MapDataCallbackPrototype callback,
      final int width,
      final int height) {
    super(library, "Itemframe Maps", file, width, height, callback);
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

    private String url;
    private int width = 5;
    private int height = 5;
    private MapDataCallbackPrototype callback;

    private Builder() {}

    public Builder url(final String url) {
      this.url = url;
      return this;
    }

    public Builder width(final int width) {
      this.width = width;
      return this;
    }

    public Builder height(final int height) {
      this.height = height;
      return this;
    }

    public Builder callback(final MapDataCallbackPrototype callback) {
      this.callback = callback;
      return this;
    }

    public MapPlayer build(@NotNull final MediaLibrary library) {
      return new MapPlayer(library, url, callback, width, height);
    }
  }
}
