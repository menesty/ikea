package org.menesty.ikea.domain.vk;

/**
 * Created by Menesty on
 * 7/19/16.
 * 23:47.
 */
public class VkPrice {
  private Long amount;
  private VKCurrency currency;
  private String text;

  public Long getAmount() {
    return amount;
  }

  public void setAmount(Long amount) {
    this.amount = amount;
  }

  public VKCurrency getCurrency() {
    return currency;
  }

  public void setCurrency(VKCurrency currency) {
    this.currency = currency;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
