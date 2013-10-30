package org.menesty.ikea.ui.controls.dialog;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.menesty.ikea.domain.User;
import org.menesty.ikea.ui.pages.DialogCallback;

public class UserDialog extends BaseDialog {
    private UserForm form;
    private DialogCallback callback;
    private User currentEntity;

    public UserDialog() {

        getChildren().addAll(form = new UserForm(), bottomBar);
    }


    public void bind(User entity, DialogCallback callback) {
        currentEntity = entity;
        form.setCombo(entity.isComboUser());
        form.setLogin(entity.getLogin());
        form.setPassword(entity.getPassword());
        this.callback = callback;
    }


    @Override
    public void onCancel() {
        if (callback != null)
            callback.onCancel();
    }

    @Override
    public void onOk() {
        currentEntity.setComboUser(form.isCombo());
        currentEntity.setLogin(form.getLogin());
        currentEntity.setPassword(form.getPassword());

        onSave(currentEntity);
    }

    private void onSave(User currentEntity) {
        if (callback != null)
            callback.onSave(currentEntity);
    }


    private class UserForm extends FormPanel {
        private final TextField login;
        private final TextField password;
        private final CheckBox combo;

        public UserForm() {
            addRow("Login", login = new TextField());
            addRow("Password", password = new TextField());
            addRow("For combo", combo = new CheckBox());
        }

        public void setLogin(String login) {
            this.login.setText(login);
        }

        public void setPassword(String password) {
            this.password.setText(password);
        }

        public void setCombo(boolean isCombo) {
            this.combo.setSelected(isCombo);
        }

        public boolean isCombo() {
            return combo.isSelected();
        }

        public String getLogin() {
            return login.getText();
        }

        public String getPassword() {
            return password.getText();
        }
    }

}
