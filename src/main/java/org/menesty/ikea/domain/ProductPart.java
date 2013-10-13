package org.menesty.ikea.domain;

/**
 * User: Menesty
 * Date: 10/12/13
 * Time: 8:59 PM
 */
public class ProductPart {

    private int count;

    private ProductInfo productInfo;

    public ProductPart() {

    }

    public ProductPart(int count, ProductInfo productInfo) {
        this.count = count;
        this.productInfo = productInfo;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    public int getBoxCount() {
        return count * productInfo.getPackageInfo().getBoxCount();
    }

    public String toString() {
        return "\n\t" + count + " | \t" + productInfo;
    }
}
