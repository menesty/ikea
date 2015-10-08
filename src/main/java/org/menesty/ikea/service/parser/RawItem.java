package org.menesty.ikea.service.parser;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 9/7/15.
 * 20:17.
 */
public class RawItem {
    private BigDecimal count;
    private String artNumber;
    private boolean combo;
    private String comment;
    private BigDecimal price;

    public RawItem() {

    }

    public RawItem(String artNumber) {
        this.artNumber = artNumber;
    }

    public BigDecimal getCount() {
        return count;
    }

    public BigDecimal getTotalPrice() {
        return count.multiply(price).setScale(2, BigDecimal.ROUND_HALF_UP);
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

    public boolean isCombo() {
        return combo;
    }

    public void setCombo(boolean combo) {
        this.combo = combo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
