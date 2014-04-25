package org.menesty.ikea.service;

import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.OrderItem;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

public class OrderItemService extends Repository<OrderItem> {
    public List<OrderItem> loadBy(CustomerOrder order) {
        boolean started = isActive();
        try {
            if (!started)
                begin();

            TypedQuery<OrderItem> query = getEm().createQuery("select entity from " + entityClass.getName() + " entity where entity.customerOrder.id = ?1", entityClass);
            query.setParameter(1, order.getId());
            return query.getResultList();

        } finally {
            if (!started)
                commit();
        }

    }

    public void removeBy(CustomerOrder order) {
        boolean started = isActive();
        if (!started)
            begin();
        remove(loadBy(order));
        if (!started)
            commit();
    }

    public static List<OrderItem> getByType(List<OrderItem> orderItems, OrderItem.Type type) {
        List<OrderItem> result = new ArrayList<>();

        for (OrderItem orderItem : orderItems)
            if (type == orderItem.getType())
                result.add(orderItem);

        return result;

    }

    public List<OrderItem> loadBy(CustomerOrder order, OrderItem.Type type) {
        boolean started = isActive();
        try {
            if (!started)
                begin();
            TypedQuery<OrderItem> query = getEm().createQuery("select entity from " + entityClass.getName() + " entity where entity.customerOrder.id = ?1 and entity.type = ?2", entityClass);
            query.setParameter(1, order.getId());
            query.setParameter(2, type);
            return query.getResultList();
        } finally {
            if (!started)
                commit();
        }
    }
}
