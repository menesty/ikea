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

    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("orderIndex")
    private List<OrderShop> orderShops = new ArrayList<>();

    @ManyToOne
    private User lackUser;

    private boolean synthetic;

    public boolean isOnline;

    private int margin;

    public int getMargin() {
        return margin;
    }

    public CustomerOrder setMargin(int margin) {
        this.margin = margin;
        return this;
    }

    public List<OrderShop> getOrderShops() {
        return orderShops;
    }

    public void setOrderShops(List<OrderShop> orderShops) {
        this.orderShops = orderShops;
    }

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

    public CustomerOrder(String name) {
        this(name, new Date());
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public CustomerOrder setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
        return this;
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

}
