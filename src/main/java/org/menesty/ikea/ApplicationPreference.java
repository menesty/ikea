package org.menesty.ikea;

import org.menesty.ikea.util.FileChooserUtil;

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

  public String getFileChooseDefaultDir(FileChooserUtil.FolderType folderType) {
    String key = "file-choose-default-dir" + (folderType != null ? "-".concat(folderType.toString().toLowerCase()) : "");
    return preferences.get(key, null);
  }

  public void setFileChooseDefaultDir(FileChooserUtil.FolderType folderType, String path) {
    String key = "file-choose-default-dir" + (folderType != null ? "-".concat(folderType.toString().toLowerCase()) : "");
    preferences.put(key, path);
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
