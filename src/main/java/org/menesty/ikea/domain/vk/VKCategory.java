package org.menesty.ikea.domain.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Menesty on
 * 7/14/16.
 * 15:17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VKCategory {
  private Long id;
  private String name;

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
}
