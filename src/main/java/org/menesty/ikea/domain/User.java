package org.menesty.ikea.domain;

public class User {

    private String login;

    private String password;

    private boolean comboUser;

    public User() {

    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public boolean isComboUser() {
        return comboUser;
    }

    public void setComboUser(boolean comboUser) {
        this.comboUser = comboUser;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
