package org.menesty.ikea.domain.vk;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Menesty on
 * 7/14/16.
 * 21:48.
 */
public class VKMarketAlbumAddResult {
  @JsonProperty("market_album_id")
  private long albumId;

  public long getAlbumId() {
    return albumId;
  }

  public void setAlbumId(long albumId) {
    this.albumId = albumId;
  }
}
