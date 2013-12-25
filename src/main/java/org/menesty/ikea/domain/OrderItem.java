package org.menesty.ikea.domain;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * User: Menesty
 * Date: 9/22/13
 * Time: 11:24 AM
 */
@Entity
public class OrderItem extends Identifiable implements UserProductInfo {

    public enum Type {
        General, Na, Specials, Combo
    }

    private boolean invalidFetch;

    private int tryCount;

    private double count;

    private String artNumber;

    private String name;
    @Column(scale = 8, precision = 2)
    private Double price;

    private String comment;
    @ManyToOne(fetch = FetchType.LAZY)
    public CustomerOrder customerOrder;

    @ManyToOne
    private ProductInfo productInfo;
    @Enumerated(EnumType.STRING)
    private Type type;

    public int getTryCount() {
        return tryCount;
    }

    public void setTryCount(int tryCount) {
        this.tryCount = tryCount;
    }

    public void increaseTryCount() {
        tryCount++;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public double getCount() {
        return count;
    }

    public boolean isInvalidFetch() {
        return invalidFetch;
    }

    public void setInvalidFetch(boolean invalidFetch) {
        this.invalidFetch = invalidFetch;
    }

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public BigDecimal getTotal() {
        return BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(price)).setScale(2, BigDecimal.ROUND_CEILING);
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderItem orderItem = (OrderItem) o;

        if (!artNumber.equals(orderItem.artNumber)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return artNumber.hashCode();
    }

    /*used in xls export*/
    public int getArtNumberAsInteger() {
        return Integer.valueOf(artNumber.replaceAll("\\D+", ""));
    }

}