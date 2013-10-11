package org.menesty.ikea.domain;

import org.menesty.ikea.order.OrderItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {

    private String name;

    private List<OrderItem> orderItems;

    private Date createdDate;

    private List<String> parseWarnings;

    public Order() {
        parseWarnings = new ArrayList<>();
        orderItems = new ArrayList<>();
    }

    public void addWarning(String message) {
        parseWarnings.add(message);
    }

    public Order(String name, Date createdDate) {
        setName(name);
        setCreatedDate(createdDate);
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getName() {
        return name;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<OrderItem> getByType(OrderItem.Type type) {
        List<OrderItem> result = new ArrayList<>();

        for (OrderItem orderItem : orderItems)
            if (type == orderItem.getType())
                result.add(orderItem);

        return result;

    }


}
