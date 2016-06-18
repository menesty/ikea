package org.menesty.ikea.ui.controls.dialog;

import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.menesty.ikea.ApplicationPreference;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.form.ComboBoxField;
import org.menesty.ikea.ui.layout.RowPanel;

import java.util.List;

public class ApplicationPreferenceDialog extends BaseDialog {

  private WarehousePreferenceForm warehouseForm;

  public ApplicationPreferenceDialog(Stage stage) {
    super(stage);
    setTitle(I18n.UA.getString(I18nKeys.APPLICATION_PREFERENCES));
    okBtn.setText(I18n.UA.getString(I18nKeys.SAVE));

    warehouseForm = new WarehousePreferenceForm();

    addRow(warehouseForm, bottomBar);
  }


  @Override
  public void onOk() {
    ApplicationPreference preference = ServiceFacade.getApplicationPreference();

    preference.setWarehouseHost(warehouseForm.getHost());
    preference.setWarehouseLogin(warehouseForm.getUser());
    preference.setWarehousePassword(warehouseForm.getPassword());
    preference.setIkeaUser(warehouseForm.getIkeaUser());
    preference.setIkeaPassword(warehouseForm.getIkeaPassword());
  }

  @Override
  public void onShow() {
    ApplicationPreference preference = ServiceFacade.getApplicationPreference();

    updateWarehouseForm();
    warehouseForm.setIkeaPassword(preference.getIkeaPassword());
    warehouseForm.setIkeaUser(preference.getIkeaUser());
    warehouseForm.setProfiles(preference.getProfiles(), preference.getActiveProfile());
  }

  private void updateWarehouseForm() {
    ApplicationPreference preference = ServiceFacade.getApplicationPreference();
    warehouseForm.setHost(preference.getWarehouseHost());
    warehouseForm.setPassword(preference.getWarehousePassword());
    warehouseForm.setUser(preference.getWarehouseUser());

  }


  class WarehousePreferenceForm extends TabPane {
    private final TextField user;
    private final PasswordField password;
    private final TextField host;
    private final TextField ikeaUser;
    private final PasswordField ikeaPassword;
    private final ComboBoxField<String> profiles;

    public WarehousePreferenceForm() {
      getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
      setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

      {
        Tab tab = new Tab("General");

        RowPanel panel = new RowPanel();

        panel.addRow(I18n.UA.getString(I18nKeys.ACTIVE_PROFILE), profiles = new ComboBoxField<>(null));
        profiles.setAllowBlank(false);
        profiles.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
          ApplicationPreference preference = ServiceFacade.getApplicationPreference();
          preference.setActiveProfile(newValue);
          updateWarehouseForm();
        });
        panel.addRow("Host :", host = new TextField());
        panel.addRow("User :", user = new TextField());
        panel.addRow("Password :", password = new PasswordField());

        tab.setContent(panel);

        getTabs().add(tab);
      }

      {
        Tab tab = new Tab("Ikea");

        RowPanel panel = new RowPanel();

        panel.addRow("User :", ikeaUser = new TextField());
        panel.addRow("Password :", ikeaPassword = new PasswordField());

        tab.setContent(panel);

        getTabs().add(tab);
      }
    }

    public void setUser(String user) {
      this.user.setText(user);
    }

    public String getUser() {
      return user.getText();
    }

    public void setHost(String host) {
      this.host.setText(host);
    }

    public String getHost() {
      return host.getText();
    }

    public void setPassword(String password) {
      this.password.setText(password);
    }

    public String getPassword() {
      return password.getText();
    }

    public String getIkeaPassword() {
      return ikeaPassword.getText();
    }

    public void setIkeaPassword(String password) {
      ikeaPassword.setText(password);
    }

    public void setIkeaUser(String user) {
      ikeaUser.setText(user);
    }

    public String getIkeaUser() {
      return ikeaUser.getText();
    }

    public void setProfiles(List<String> profiles, String activeProfile) {
      this.profiles.setItems(profiles);
      this.profiles.setValue(activeProfile);
    }
  }
}
