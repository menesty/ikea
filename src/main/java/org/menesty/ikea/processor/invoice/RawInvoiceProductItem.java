package org.menesty.ikea.processor.invoice;

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

    private String priceStr;

    private String wat;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPriceStr() {
        return priceStr;
    }

    public void setPriceStr(String priceStr) {
        this.priceStr = priceStr;
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
        return Double.valueOf(priceStr.trim().replace(",", "."));
    }
}
