package org.menesty.ikea.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class CustomerOrder extends Identifiable {

    private String name;
    @Temporal(TemporalType.DATE)
    private Date createdDate;
    @ElementCollection
    private List<String> parseWarnings = new ArrayList<>();

    @OneToMany
    private List<User> users;
    @OneToMany
    private List<IkeaShop> ikeaShops;

    @ManyToOne
    private User lackUser;

    public boolean isOnline;

    public User getLackUser() {
        return lackUser;
    }

    public void setLackUser(User lackUser) {
        this.lackUser = lackUser;
    }

    public List<String> getParseWarnings() {
        return parseWarnings;
    }

    public void setParseWarnings(List<String> parseWarnings) {
        this.parseWarnings = parseWarnings;
    }

    public CustomerOrder() {
    }

    public void addWarning(String message) {
        parseWarnings.add(message);
    }

    public CustomerOrder(String name, Date createdDate) {
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

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<IkeaShop> getIkeaShops() {
        return ikeaShops;
    }

    public void setIkeaShops(List<IkeaShop> ikeaShops) {
        this.ikeaShops = ikeaShops;
    }
}
