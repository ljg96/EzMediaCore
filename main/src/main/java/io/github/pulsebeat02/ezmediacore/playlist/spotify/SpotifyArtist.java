package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import io.github.pulsebeat02.ezmediacore.throwable.UnknownArtistException;
import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;

public class SpotifyArtist implements Artist {

  private final com.wrapper.spotify.model_objects.specification.Artist artist;
  private final String url;
  private final Avatar[] avatars;

  public SpotifyArtist(@NotNull final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    this.url = url;
    this.artist =
        SpotifyProvider.getSpotifyApi()
            .getArtist(
                MediaExtractionUtils.getSpotifyID(url)
                    .orElseThrow(() -> new UnknownArtistException(url)))
            .build()
            .execute();
    this.avatars =
        Arrays.stream(this.artist.getImages())
            .map(SpotifyAvatar::new)
            .toArray(SpotifyAvatar[]::new);
  }

  SpotifyArtist(@NotNull final ArtistSimplified simplified)
      throws IOException, ParseException, SpotifyWebApiException {
    this(simplified.getUri());
  }

  @Override
  public @NotNull String getId() {
    return this.artist.getId();
  }

  @Override
  public @NotNull String getName() {
    return this.artist.getName();
  }

  @Override
  public @NotNull Map<String, String> getExternalUrls() {
    return this.artist.getExternalUrls().getExternalUrls();
  }

  @Override
  public int getTotalFollowers() {
    return this.artist.getFollowers().getTotal();
  }

  @Override
  public int getPopularity() {
    return this.artist.getPopularity();
  }

  @Override
  public @NotNull String[] getGenres() {
    return this.artist.getGenres();
  }

  @Override
  public @NotNull Avatar[] getImages() {
    return this.avatars;
  }

  @Override
  public @NotNull String getUrl() {
    return this.url;
  }

  @NotNull com.wrapper.spotify.model_objects.specification.Artist getArtist() {
    return this.artist;
  }
}
