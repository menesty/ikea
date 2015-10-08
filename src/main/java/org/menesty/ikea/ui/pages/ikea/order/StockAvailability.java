package org.menesty.ikea.ui.pages.ikea.order;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 10/1/15.
 * 23:33.
 */
public class StockAvailability {
    private int shopId;

    private BigDecimal available;

    private BigDecimal available2;

    private BigDecimal available3;

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    public void setAvailable2(BigDecimal available2) {
        this.available2 = available2;
    }

    public BigDecimal getAvailable2() {
        return available2;
    }

    public BigDecimal getAvailable3() {
        return available3;
    }

    public void setAvailable3(BigDecimal available3) {
        this.available3 = available3;
    }

    @Override
    public String toString() {
        return "StockAvailability{" +
                "shopId=" + shopId +
                ", available=" + available +
                ", available2=" + available2 +
                ", available3=" + available3 +
                '}';
    }
}