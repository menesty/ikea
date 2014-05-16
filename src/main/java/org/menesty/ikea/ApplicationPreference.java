package org.menesty.ikea;

import java.util.prefs.Preferences;

public class ApplicationPreference {

    private Preferences preferences;

    public ApplicationPreference() {
        preferences = Preferences.userNodeForPackage(ApplicationPreference.class);
    }

    public void setWarehouseHost(String host) {
        preferences.put("warehouse-host", host);
    }

    public void setWarehouseLogin(String user) {
        preferences.put("warehouse-user", user);
    }

    public void setWarehousePassword(String password) {
        preferences.put("warehouse-password", password);
    }

    public String getWarehouseHost() {
        return preferences.get("warehouse-host", "http://localhost");
    }

    public String getWarehouseUser() {
        return preferences.get("warehouse-user", "");
    }

    public String getWarehousePassword() {
        return preferences.get("warehouse-password", "");
    }

    public String getFileChooseDefaultDir() {
        return preferences.get("file-choose-default-dir", null);
    }

    public void setFileChooseDefaultDir(String path) {
        preferences.put("file-choose-default-dir", path);
    }

    public void setIkeaUser(String user) {
        preferences.put("ikea-user", user);
    }

    public String getIkeaUser() {
        return preferences.get("ikea-user", "");
    }

    public String getIkeaPassword() {
        return preferences.get("ikea-password", "");
    }

    public void setIkeaPassword(String password) {
        preferences.put("ikea-password", password);
    }
}
