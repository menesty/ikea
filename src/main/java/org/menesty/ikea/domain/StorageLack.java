package org.menesty.ikea.domain;

public class StorageLack {

    private double count;

    private boolean exist = true;

    private ProductInfo productInfo;


    public StorageLack(ProductInfo productInfo, double count) {
        this(productInfo, count, true);
    }

    public StorageLack(ProductInfo productInfo, double count, boolean exist) {
        setProductInfo(productInfo);
        setCount(count);
        setExist(exist);
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }
}
