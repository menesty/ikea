package org.menesty.ikea.order;

import org.menesty.ikea.domain.ProductInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * User: Menesty
 * Date: 9/22/13
 * Time: 11:24 AM
 */
public class OrderItem {

    private boolean aNew;

    public void setNew(boolean aNew) {
        this.aNew = aNew;
    }

    public boolean isNew() {
        return aNew;
    }

    public enum Type {
        General, Na, Specials, Combo
    }

    private int count;
    private String artNumber;
    private String name;
    private Double price;
    private String comment;
    private String group;

    private ProductInfo productInfo;

    private Type type;

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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    public Double getTotal() {
        double value = (double) count * price;
        return value > 0 ? BigDecimal.valueOf(value).setScale(2, RoundingMode.CEILING).doubleValue() : 0d;
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

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
