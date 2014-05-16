package org.menesty.ikea.service;

import org.menesty.ikea.domain.IkeaParagon;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Date;
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

    public void setUploaded(Date date, String name) {
        boolean started = isActive();

        if (!started)
            begin();

        Query query = getEm().createQuery("update " + entityClass.getName() + " entity set entity.uploaded = true where entity.name = ?1 and entity.createdDate = ?2");
        query.setParameter(1, name);
        query.setParameter(2, date);

        query.executeUpdate();

        if (!started)
            commit();

    }
}
