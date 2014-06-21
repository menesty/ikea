package org.menesty.ikea.service;

import org.menesty.ikea.domain.User;

import javax.persistence.TypedQuery;
import java.util.List;

public class UserService extends Repository<User> {

    public List<User> load(boolean comboUser) {
        boolean started = isActive();

        if (!started)
            begin();

        TypedQuery<User> query = getEm().createQuery("select entity from " + entityClass.getName() + " entity where entity.comboUser = ?1", entityClass);
        query.setParameter(1, comboUser);
        List<User> result = query.getResultList();

        if (!started)
            commit();

        return result;
    }
}
