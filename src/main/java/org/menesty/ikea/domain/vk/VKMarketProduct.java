package org.menesty.ikea.domain.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 7/19/16.
 * 17:54.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VKMarketProduct {
  private Long id;
  private String name;
  private String description;
  private Long categoryId;
  private BigDecimal price;
  private boolean deleted;
  private Long mainPhotoId;
  private String photoIds;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public Long getMainPhotoId() {
    return mainPhotoId;
  }

  public void setMainPhotoId(Long mainPhotoId) {
    this.mainPhotoId = mainPhotoId;
  }

  public String getPhotoIds() {
    return photoIds;
  }

  public void setPhotoIds(String photoIds) {
    this.photoIds = photoIds;
  }
}
