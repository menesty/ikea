package org.menesty.ikea.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty on 1/11/14.
 */
public class StorageComboLack {

    public StorageComboLack(ProductInfo productInfo) {
        setProductInfo(productInfo);
    }

    private ProductInfo productInfo;

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    public List<StorageComboPartLack> storageComboLacks = new ArrayList<>();
}