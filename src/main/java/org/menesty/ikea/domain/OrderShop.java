package org.menesty.ikea.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * Created by Menesty on
 * 8/16/14.
 * 15:54.
 */
@Entity
public class OrderShop extends Identifiable {
    @ManyToOne(fetch = FetchType.LAZY)
    private CustomerOrder customerOrder;

    @ManyToOne
    private IkeaShop ikeaShop;

    private int orderIndex;

    public OrderShop() {

    }

    public OrderShop(CustomerOrder customerOrder, IkeaShop ikeaShop) {
        this.ikeaShop = ikeaShop;
        this.customerOrder = customerOrder;
    }

    public CustomerOrder getCustomerOrder() {
        return customerOrder;
    }

    public void setCustomerOrder(CustomerOrder customerOrder) {
        this.customerOrder = customerOrder;
    }

    public IkeaShop getIkeaShop() {
        return ikeaShop;
    }

    public void setIkeaShop(IkeaShop ikeaShop) {
        this.ikeaShop = ikeaShop;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderShop orderShop = (OrderShop) o;

        if (orderIndex != orderShop.orderIndex) return false;

        if (ikeaShop != null ? !ikeaShop.equals(orderShop.ikeaShop) : orderShop.ikeaShop != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return ikeaShop.getName();
    }
}
