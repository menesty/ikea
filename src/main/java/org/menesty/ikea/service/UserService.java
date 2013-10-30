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

    public List<User> getGeneral() {
        User example = new User();
        example.setComboUser(true);
        return DatabaseService.get().queryByExample(example);
    }

    public List<User> getCombos() {
        return DatabaseService.get().queryByExample(new User());
    }
}
