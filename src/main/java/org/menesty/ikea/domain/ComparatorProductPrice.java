package org.menesty.ikea.domain;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 6/13/16.
 * 14:12.
 */
public class ComparatorProductPrice {
  private String artNumber;

  private BigDecimal priceRu;

  private BigDecimal pricePl;

  private BigDecimal priceLt;

  private BigDecimal priceRo;

  public ComparatorProductPrice() {
  }

  public BigDecimal getPriceRo() {
    return priceRo;
  }

  public void setPriceRo(BigDecimal priceRo) {
    this.priceRo = priceRo;
  }

  public ComparatorProductPrice(String artNumber) {
    this.artNumber = artNumber;
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
