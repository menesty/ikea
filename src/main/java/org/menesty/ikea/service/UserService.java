package org.menesty.ikea.service;

import org.menesty.ikea.domain.User;

import java.util.List;

public class UserService {

    public List<User> load() {
        return DatabaseService.get().query(User.class);
    }

    public void save(User entity) {
        DatabaseService.get().store(entity);
    }
}
