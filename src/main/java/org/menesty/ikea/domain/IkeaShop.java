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

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Identifiable)) return false;

        Identifiable that = (Identifiable) o;

        return getId().equals(that.getId());
    }
}
