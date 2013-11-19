package org.menesty.ikea.domain;

public class StorageLack implements UserProductInfo {

    private double count;

    private boolean exist = true;

    private final ProductInfo productInfo;


    public StorageLack(ProductInfo productInfo, double count) {
        this(productInfo, count, true);
    }

    public StorageLack(ProductInfo productInfo, double count, boolean exist) {
        this.productInfo = productInfo;
        setCount(count);
        setExist(exist);
    }

    @Override
    public String getArtNumber() {
        return productInfo.getOriginalArtNum();
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

}