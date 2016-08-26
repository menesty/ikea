package org.menesty.ikea.domain.vk;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Menesty on
 * 7/14/16.
 * 21:48.
 */
public class VKUploadServer {
  @JsonProperty("upload_url")
  private String uploadServerUrl;

  public String getUploadServerUrl() {
    return uploadServerUrl;
  }

  public void setUploadServerUrl(String uploadServerUrl) {
    this.uploadServerUrl = uploadServerUrl;
  }
}
