package org.menesty.ikea.domain;

public class ParagonItemDto {

    private int paragonId;

    private String productNumber;

    private double count;

    private double price;

    private String shortName;

    public int getParagonId() {
        return paragonId;
    }

    public void setParagonId(int paragonId) {
        this.paragonId = paragonId;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
