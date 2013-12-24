package org.menesty.ikea.service;

import org.menesty.ikea.db.DatabaseService;
import org.menesty.ikea.domain.Identifiable;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty on 12/21/13.
 */
public abstract class Repository<T extends Identifiable> {
    protected final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public Repository() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        entityClass = (Class<T>) type.getActualTypeArguments()[0];
    }

    public List<T> load() {
        try {
            begin();
            String queryString = "select entity from " + entityClass.getName() + " entity";
            TypedQuery<T> query = getEm().createQuery(queryString, entityClass);
            return query.getResultList();
        } finally {
            commit();
        }
    }

    public EntityManager getEm() {
        return DatabaseService.getEntityManager();
    }

    public <E extends Identifiable> E save(E entity) {
        boolean alreadyStarted = isActive();
        if (!alreadyStarted)
            begin();

        if (entity.getId() == null)
            getEm().persist(entity);
        else
            entity = getEm().merge(entity);

        if (!alreadyStarted)
            commit();
        return entity;
    }

    public <E extends Identifiable> List<E> save(List<E> entities) {
        List<E> result = new ArrayList<>();
        for (E entity : entities)
            result.add(save(entity));

        return result;

    }

    public <E extends Identifiable> void remove(List<E> list) {
        boolean started = isActive();
        try {
            if (!started)
                begin();

            for (E item : list)
                remove(item);

            if (!started)
                commit();

        } catch (Exception e) {
            rollback();
        }
    }


    public <E extends Identifiable> void remove(E entity) {
        boolean started = isActive();
        if (!started)
            begin();

        getEm().remove(entity);

        if (!started)
            commit();
    }

    protected void begin() {
        DatabaseService.begin();
    }

    protected boolean isActive() {
        return DatabaseService.isActive();
    }

    protected void commit() {
        DatabaseService.commit();
    }

    protected void rollback() {
        DatabaseService.rollback();
    }
}
