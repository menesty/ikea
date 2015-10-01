package org.menesty.ikea.db;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 9/23/15.
 * 12:41.
 */
public class Item {
    private String artNumber;
    private BigDecimal plPrice;
    private BigDecimal trPrice;
    private BigDecimal count;

    public BigDecimal getCount() {
        return count;
    }

    public void setCount(BigDecimal count) {
        this.count = count;
    }

    public String getArtNumber() {
        return artNumber;
    }

    public void setArtNumber(String artNumber) {
        this.artNumber = artNumber;
    }

    public BigDecimal getPlPrice() {
        return plPrice;
    }

    public void setPlPrice(BigDecimal plPrice) {
        this.plPrice = plPrice;
    }

    public BigDecimal getTrPrice() {
        return trPrice;
    }

    public void setTrPrice(BigDecimal trPrice) {
        this.trPrice = trPrice;
    }
}
