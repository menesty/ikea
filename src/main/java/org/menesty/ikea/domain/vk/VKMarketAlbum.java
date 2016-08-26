package org.menesty.ikea.domain.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by Menesty on
 * 7/14/16.
 * 14:13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VKMarketAlbum {
  private Long id;
  private String title;
  private Long photoId;

  private VKPhoto photo;

  @JsonProperty("updated_time")
  private Date updatedTime;

  public VKPhoto getPhoto() {
    return photo;
  }

  public void setPhoto(VKPhoto photo) {
    this.photo = photo;
  }

  public Date getUpdatedTime() {
    return updatedTime;
  }

  public void setUpdatedTime(Date updatedTime) {
    this.updatedTime = updatedTime;
  }

  public Long getPhotoId() {
    return photoId;
  }

  public void setPhotoId(Long photoId) {
    this.photoId = photoId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return title;
  }
}
