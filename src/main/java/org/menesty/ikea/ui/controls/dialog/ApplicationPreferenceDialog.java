package org.menesty.ikea.ui.controls.dialog;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.menesty.ikea.ApplicationPreference;
import org.menesty.ikea.service.ServiceFacade;

public class ApplicationPreferenceDialog extends BaseDialog {

    private WarehousePreferenceForm warehouseForm;

    public ApplicationPreferenceDialog() {
        addRow(createTitle("Application preferences"));
        okBtn.setText("Save");
        warehouseForm = new WarehousePreferenceForm();
        addRow(warehouseForm, bottomBar);
    }


    @Override
    public void onOk() {
        ApplicationPreference preference = ServiceFacade.getApplicationPreference();
        preference.setWarehouseHost(warehouseForm.getHost());
        preference.setWarehouseLogin(warehouseForm.getUser());
        preference.setWarehousePassword(warehouseForm.getPassword());

    }

    @Override
    public void onShow() {
        ApplicationPreference preference = ServiceFacade.getApplicationPreference();
        warehouseForm.setHost(preference.getWarehouseHost());
        warehouseForm.setPassword(preference.getWarehousePassword());
        warehouseForm.setUser(preference.getWarehouseUser());
    }


    class WarehousePreferenceForm extends FormPanel {
        private final TextField user;
        private final TextField password;
        private final TextField host;

        public WarehousePreferenceForm() {
            addRow("Host :", host = new TextField());
            addRow("User :", user = new TextField());
            addRow("Password :", password = new PasswordField());
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
    }
}
