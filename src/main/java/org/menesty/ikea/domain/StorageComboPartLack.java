package org.menesty.ikea.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Menesty on 1/11/14.
 */
public class StorageComboPartLack {

    private ProductInfo productInfo;

    private int total;

    private int lackCount;

    public StorageComboPartLack(ProductInfo productInfo, int total) {
        setTotal(total);
        setProductInfo(productInfo);
    }

    public boolean isLack() {
        return lackCount != 0;
    }

    public double getTotalLackPrice() {
        return BigDecimal.valueOf(lackCount).multiply(BigDecimal.valueOf(productInfo.getPrice())).setScale(2, RoundingMode.CEILING).doubleValue();
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getLackCount() {
        return lackCount;
    }

    public void setLackCount(int lackCount) {
        this.lackCount = lackCount;
    }

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }
}
