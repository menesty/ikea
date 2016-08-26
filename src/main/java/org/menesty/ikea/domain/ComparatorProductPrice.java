package org.menesty.ikea.domain;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 6/13/16.
 * 14:12.
 */
public class ComparatorProductPrice {
  private String artNumber;

  private BigDecimal count = BigDecimal.ZERO;

  private BigDecimal priceRu = BigDecimal.ZERO;

  private BigDecimal pricePl = BigDecimal.ZERO;

  private BigDecimal priceLt = BigDecimal.ZERO;

  private BigDecimal priceRo = BigDecimal.ZERO;

  private BigDecimal priceHu = BigDecimal.ZERO;

  public ComparatorProductPrice() {
  }

  public BigDecimal getCount() {
    return count;
  }

  public void setCount(BigDecimal count) {
    this.count = count;
  }

  public BigDecimal getPriceHu() {
    return priceHu;
  }

  public void setPriceHu(BigDecimal priceHu) {
    this.priceHu = priceHu;
  }

  public BigDecimal getPriceRo() {
    return priceRo;
  }

  public void setPriceRo(BigDecimal priceRo) {
    this.priceRo = priceRo;
  }

  public ComparatorProductPrice(String artNumber, BigDecimal count) {
    this.artNumber = artNumber;
    this.count = count;
  }

  public String getArtNumber() {
    return artNumber;
  }

  public void setArtNumber(String artNumber) {
    this.artNumber = artNumber;
  }

  public BigDecimal getPriceRu() {
    return priceRu;
  }

  public void setPriceRu(BigDecimal priceRu) {
    this.priceRu = priceRu;
  }

  public BigDecimal getPricePl() {
    return pricePl;
  }

  public void setPricePl(BigDecimal pricePl) {
    this.pricePl = pricePl;
  }

  public BigDecimal getPriceLt() {
    return priceLt;
  }

  public void setPriceLt(BigDecimal priceLt) {
    this.priceLt = priceLt;
  }
}
