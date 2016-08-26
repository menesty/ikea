package org.menesty.ikea.domain.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Menesty on
 * 7/14/16.
 * 14:36.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VKData<T> {
  private int count;
  private List<T> items;

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public List<T> getItems() {
    return items;
  }

  public void setItems(List<T> items) {
    this.items = items;
  }
}