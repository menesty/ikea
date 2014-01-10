package org.menesty.ikea.service;

import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.OrderItem;

import javax.persistence.TypedQuery;
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
        begin();
        remove(loadBy(order));
        commit();
    }
}
