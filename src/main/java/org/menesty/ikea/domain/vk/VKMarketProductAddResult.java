package org.menesty.ikea.domain.vk;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Menesty on
 * 7/14/16.
 * 21:48.
 */
public class VKMarketProductAddResult {
  @JsonProperty("market_item_id")
  private long itemId;

  public long getItemId() {
    return itemId;
  }

  public void setItemId(long itemId) {
    this.itemId = itemId;
  }
}
