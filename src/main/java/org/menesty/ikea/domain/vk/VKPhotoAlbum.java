package org.menesty.ikea.domain.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Menesty on
 * 7/14/16.
 * 23:08.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VKPhotoAlbum {
  private long id;
  @JsonProperty("thumb_id")
  private long thumbId;
  @JsonProperty("owner_id")
  private long ownerId;
  private int size;
  @JsonProperty("thumb_src")
  private String thumbSrc;

  private String title;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getThumbId() {
    return thumbId;
  }

  public void setThumbId(long thumbId) {
    this.thumbId = thumbId;
  }

  public long getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(long ownerId) {
    this.ownerId = ownerId;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public String getThumbSrc() {
    return thumbSrc;
  }

  public void setThumbSrc(String thumbSrc) {
    this.thumbSrc = thumbSrc;
  }
}
