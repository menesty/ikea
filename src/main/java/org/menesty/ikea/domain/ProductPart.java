package org.menesty.ikea.domain;

import javax.persistence.*;

/**
 * User: Menesty
 * Date: 10/12/13
 * Time: 8:59 PM
 */
@Entity
public class ProductPart extends Identifiable {
    @Column(name = "count_items")
    private int count;
    @ManyToOne(fetch = FetchType.LAZY)
    public ProductInfo parent;

    @ManyToOne(cascade = CascadeType.ALL)
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
