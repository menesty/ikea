package org.menesty.ikea.domain.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Menesty on
 * 7/14/16.
 * 17:13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VKUploadPhotoProductResult {
  private int server;
  private String photo;
  private String hash;
  @JsonProperty("crop_data")
  private String cropData;
  @JsonProperty("crop_hash")
  private String cropHash;

  public int getServer() {
    return server;
  }

  public void setServer(int server) {
    this.server = server;
  }

  public String getPhoto() {
    return photo;
  }

  public void setPhoto(String photo) {
    this.photo = photo;
  }

  public String getCropData() {
    return cropData;
  }

  public void setCropData(String cropData) {
    this.cropData = cropData;
  }

  public String getCropHash() {
    return cropHash;
  }

  public void setCropHash(String cropHash) {
    this.cropHash = cropHash;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }
}
