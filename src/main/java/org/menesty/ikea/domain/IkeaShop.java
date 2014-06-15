package org.menesty.ikea.domain;

import javax.persistence.Entity;

/**
 * Created by Menesty on
 * 6/6/14.
 * 19:32.
 */
@Entity
public class IkeaShop extends Identifiable {

    private String name;

    private int shopId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }
}
