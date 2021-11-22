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
package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import com.wrapper.spotify.SpotifyApi;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import org.jetbrains.annotations.NotNull;

public final class SpotifyProvider {

  private static SpotifyApi SPOTIFY_API;

  private SpotifyProvider() {}

  public static void init(@NotNull final MediaLibraryCore core) {
    if (isSpecified(core)) {
      SPOTIFY_API = createApi(core.getSpotifyClient());
    }
  }

  private static boolean isSpecified(@NotNull final MediaLibraryCore core) {
    return core.getSpotifyClient() != null;
  }

  private static @NotNull SpotifyApi createApi(@NotNull final SpotifyClient client) {
    return new SpotifyApi.Builder()
        .setClientId(client.getClientID())
        .setClientSecret(client.getClientSecret())
        .build();
  }

  static SpotifyApi getSpotifyApi() {
    return SPOTIFY_API;
  }
}
