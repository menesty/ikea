package org.menesty.ikea;

import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

public class ApplicationPreference {

  private static final String DEFAULT_PROFILE = "default";

  private Preferences preferences;

  public ApplicationPreference() {
    preferences = Preferences.userNodeForPackage(ApplicationPreference.class);
  }

  public void setWarehouseHost(String host) {
    preferences.put(getActiveProfile() + ".warehouse-host", host);
  }

  public void setWarehouseLogin(String user) {
    preferences.put(getActiveProfile() + ".warehouse-user", user);
  }

  public void setWarehousePassword(String password) {
    preferences.put(getActiveProfile() + ".warehouse-password", password);
  }

  public String getWarehouseHost() {
    return preferences.get(getActiveProfile() + ".warehouse-host", "http://localhost");
  }

  public String getWarehouseUser() {
    return preferences.get(getActiveProfile() + ".warehouse-user", "");
  }

  public String getWarehousePassword() {
    return preferences.get(getActiveProfile() + ".warehouse-password", "");
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

  public String getActiveProfile() {
    return preferences.get("activeProfile", DEFAULT_PROFILE);
  }

  public void setActiveProfile(String profile) {
    preferences.put("activeProfile", profile);
  }

  public List<String> getProfiles() {
    String profiles = preferences.get("profiles", DEFAULT_PROFILE + ",site");

    return Arrays.asList(profiles.split(","));
  }
}
