package org.menesty.ikea.domain.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.menesty.ikea.domain.PhotoUrl;

/**
 * Created by Menesty on
 * 7/14/16.
 * 23:48.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VKPhoto implements PhotoUrl {
  private long id;
  @JsonProperty("album_id")
  private long albumId;
  @JsonProperty("owner_id")
  private long ownerId;
  @JsonProperty("user_id")
  private long userId;
  @JsonProperty("photo_75")
  private String photo75;
  @JsonProperty("photo_130")
  private String photo130;
  @JsonProperty("photo_604")
  private String photo604;
  private String text;
  @JsonProperty("photo_807")
  private String photo807;

  @JsonProperty("post_id")
  private long postId;

  public String getPhoto807() {
    return photo807;
  }

  public void setPhoto807(String photo807) {
    this.photo807 = photo807;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getAlbumId() {
    return albumId;
  }

  public void setAlbumId(long albumId) {
    this.albumId = albumId;
  }

  public long getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(long ownerId) {
    this.ownerId = ownerId;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public String getPhoto75() {
    return photo75;
  }

  public void setPhoto75(String photo75) {
    this.photo75 = photo75;
  }

  public String getPhoto130() {
    return photo130;
  }

  public void setPhoto130(String photo130) {
    this.photo130 = photo130;
  }

  public String getPhoto604() {
    return photo604;
  }

  public void setPhoto604(String photo604) {
    this.photo604 = photo604;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public long getPostId() {
    return postId;
  }

  public void setPostId(long postId) {
    this.postId = postId;
  }

  @Override
  public String getUrl() {
    return photo130;
  }
}
