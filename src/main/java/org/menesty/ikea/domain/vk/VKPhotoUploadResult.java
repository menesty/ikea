package org.menesty.ikea.domain.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by Menesty on
 * 7/19/16.
 * 19:12.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VKPhotoUploadResult {
  private Long id;
  @JsonProperty("album_id")
  private Long albumId;
  @JsonProperty("owner_id")
  private Long ownerId;
  @JsonProperty("user_id")
  private Long userId;
  @JsonProperty("photo75")
  private String photo_75;
  @JsonProperty("photo_130")
  private String photo130;
  @JsonProperty("photo_604")
  private String photo604;
  private int width;
  private int height;
  private String text;
  private Date date;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getAlbumId() {
    return albumId;
  }

  public void setAlbumId(Long albumId) {
    this.albumId = albumId;
  }

  public Long getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(Long ownerId) {
    this.ownerId = ownerId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getPhoto_75() {
    return photo_75;
  }

  public void setPhoto_75(String photo_75) {
    this.photo_75 = photo_75;
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

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
