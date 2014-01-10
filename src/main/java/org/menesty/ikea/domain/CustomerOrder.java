package org.menesty.ikea.domain;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.List;

@Entity
public class CustomerOrder extends Identifiable {

    private String name;

    private Date createdDate;
    @ElementCollection
    private List<String> parseWarnings;

    @ManyToOne
    private User generalUser;
    @ManyToOne
    private User comboUser;
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

    public User getGeneralUser() {
        return generalUser;
    }

    public void setGeneralUser(User generalUser) {
        this.generalUser = generalUser;
    }

    public User getComboUser() {
        return comboUser;
    }

    public void setComboUser(User comboUser) {
        this.comboUser = comboUser;
    }

}
