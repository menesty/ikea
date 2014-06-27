package org.menesty.ikea.service;

import org.menesty.ikea.db.DatabaseService;
import org.menesty.ikea.domain.Identifiable;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty
 * on 12/21/13.
 */
public abstract class Repository<T extends Identifiable> {
    protected final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public Repository() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        entityClass = (Class<T>) type.getActualTypeArguments()[0];
    }

    public List<T> load() {
        boolean started = isActive();

        if (!started)
            begin();

        String queryString = "select entity from " + entityClass.getName() + " entity";
        TypedQuery<T> query = getEm().createQuery(queryString, entityClass);
        List<T> result = query.getResultList();

        if (!started)
            commit();

        return result;
    }

    public List<T> load(int offset, int limit) {
        return load(offset, limit, new OrderBy("id", OrderBy.Direction.desc));
    }

    public List<T> load(int offset, int limit, OrderBy orderBy) {
        boolean started = isActive();

        if (!started)
            begin();

        String queryString = "select entity from " + entityClass.getName() + " entity order by entity."
                + orderBy.field + " " + orderBy.direction.toString() + " ";
        TypedQuery<T> query = getEm().createQuery(queryString, entityClass);
        query.setMaxResults(limit);
        query.setFirstResult(offset);

        List<T> result = query.getResultList();

        if (!started)
            commit();

        return result;
    }

    public long count() {
        boolean started = isActive();

        if (!started)
            begin();

        String queryString = "select count(entity) from " + entityClass.getName() + " entity";
        TypedQuery<Long> query = getEm().createQuery(queryString, Long.class);
        Long result = query.getSingleResult();

        if (!started)
            commit();

        return result;
    }

    public EntityManager getEm() {
        return DatabaseService.getEntityManager();
    }

    public <E extends Identifiable> E save(E entity) {
        boolean alreadyStarted = isActive();
        try {
            if (!alreadyStarted)
                begin();

            if (entity.getId() == null)
                getEm().persist(entity);
            else
                entity = getEm().merge(entity);

            if (!alreadyStarted)
                commit();
        } catch (Exception e) {
            if (!alreadyStarted)
                rollback();
            throw new RuntimeException(e);
        }
        return entity;
    }

    public <E extends Identifiable> List<E> save(List<E> entities) {
        boolean started = isActive();
        List<E> result = new ArrayList<>();
        try {
            if (!started)
                begin();

            for (E entity : entities)
                result.add(save(entity));

            if (!started)
                commit();
        } catch (Exception e) {
            if (!started)
                rollback();
            throw new RuntimeException(e);
        }
        return result;

    }

    public void remove(List<T> list) {
        remove(list, entityClass);
    }

    public <E extends Identifiable> void remove(List<E> list, Class<E> clazz) {
        boolean started = isActive();
        try {
            if (!started)
                begin();

            for (E item : list)
                remove(item, clazz);

            if (!started)
                commit();

        } catch (Exception e) {
            rollback();
        }
    }

    public void remove(T entity) {
        remove(entity, entityClass);
    }

    public <E extends Identifiable> void remove(E entity, Class<E> clazz) {
        boolean started = isActive();
        try {
            if (!started)
                begin();

            Query query = getEm().createQuery("delete from " + clazz.getName() + " entity where entity.id=?1");
            query.setParameter(1, entity.getId());
            query.executeUpdate();

            if (!started)
                commit();

        } catch (Exception e) {
            rollback();
            throw new RuntimeException("Can not delete item");
        }
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

    static class OrderBy {
        enum Direction {
            desc, asc
        }

        public OrderBy(String field, Direction direction) {
            this.field = field;
            this.direction = direction;
        }

        public String field;

        public Direction direction = Direction.asc;
    }
}
