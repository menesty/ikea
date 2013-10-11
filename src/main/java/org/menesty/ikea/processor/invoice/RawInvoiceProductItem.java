package org.menesty.ikea.processor.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * User: Menesty
 * Date: 9/23/13
 * Time: 11:03 PM
 */
public class RawInvoiceProductItem {

    private String originalArtNumber;

    private String artNumber;

    private String name;

    private int count;

    private String comment;

    private double price;

    private String wat;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getTotal() {
        return BigDecimal.valueOf(getPrice()).setScale(2, RoundingMode.CEILING).doubleValue();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setPriceStr(String priceStr) {
        price = Double.valueOf(priceStr.trim().replaceAll("[\\s\\u00A0]+", "").replace(",", "."));
    }

    public String getWat() {
        return wat;
    }

    public void setWat(String wat) {
        this.wat = wat;
    }

    public String getOriginalArtNumber() {
        return originalArtNumber;
    }

    public void setOriginalArtNumber(String originalArtNumber) {
        this.originalArtNumber = originalArtNumber;
    }

    public String getArtNumber() {
        return artNumber;
    }

    public void setArtNumber(String artNumber) {
        this.artNumber = artNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return BigDecimal.valueOf(price).setScale(2).doubleValue();
    }

    @Override
    public String toString() {
        return artNumber + ";" + name + ";" + count + ";" + wat + ";" + String.valueOf(getPrice()).replace(".",",") + ";" + String.valueOf(getTotal()).replace(".",",");
    }
}
