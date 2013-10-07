package org.menesty.ikea.domain;

import org.menesty.ikea.order.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private String name;

    private List<OrderItem> orderItems;

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }


    public List<OrderItem> getByType(OrderItem.Type type) {
        List<OrderItem> result = new ArrayList<>();

        for (OrderItem orderItem : orderItems)
            if (type == orderItem.getType())
                result.add(orderItem);

        return result;

    }


}
