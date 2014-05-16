package org.menesty.ikea.service;

import org.menesty.ikea.domain.IkeaParagon;
import org.menesty.ikea.processor.invoice.InvoiceItem;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by Menesty on 5/14/14.
 */
public class IkeaParagonService extends Repository<IkeaParagon> {
    public List<IkeaParagon> load(int offset, int limit) {
        return load(offset, limit, new OrderBy("createdDate", OrderBy.Direction.desc));
    }

    public IkeaParagon findByName(String name) {
        boolean started = isActive();

        if (!started)
            begin();

        IkeaParagon result;

        TypedQuery<IkeaParagon> query = getEm().createQuery("select entity from " + entityClass.getName() + " entity where entity.name = ?1", entityClass);
        query.setParameter(1, name);
        query.setMaxResults(1);

        try {
            result = query.getSingleResult();
        } catch (NoResultException e) {
            result = null;
        }
        if (!started)
            commit();

        return result;
    }
}
