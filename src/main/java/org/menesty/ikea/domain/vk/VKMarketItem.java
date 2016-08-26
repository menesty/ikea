package org.menesty.ikea.domain.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Menesty on
 * 7/19/16.
 * 17:54.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VKMarketItem {
  private Long id;
  private String title;
  private String description;
  private VKCategory category;
  private VkPrice price;
  private boolean deleted;
  private List<VKPhoto> photos;

  public List<VKPhoto> getPhotos() {
    return photos;
  }

  public void setPhotos(List<VKPhoto> photos) {
    this.photos = photos;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public VKCategory getCategory() {
    return category;
  }

  public void setCategory(VKCategory category) {
    this.category = category;
  }

  public VkPrice getPrice() {
    return price;
  }

  public void setPrice(VkPrice price) {
    this.price = price;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

}
