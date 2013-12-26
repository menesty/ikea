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

}
