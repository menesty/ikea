package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.User;
import org.menesty.ikea.service.UserService;
import org.menesty.ikea.ui.pages.EntityDialogCallback;

import java.util.List;

public class OrderEditDialog extends BaseDialog {

    private OrderForm form;

    private UserService userService;

    private CustomerOrder currentEntity;

    private EntityDialogCallback<CustomerOrder> callback;


    public OrderEditDialog() {
        userService = new UserService();

        okBtn.setDisable(true);
        getChildren().add(createTitle("Edit order"));

        form = new OrderForm();

        getChildren().addAll(form, bottomBar);
    }

    @Override
    public void onOk() {
        currentEntity.setName(form.getOrderName());
        currentEntity.setGeneralUser(form.getGeneralUser());
        currentEntity.setComboUser(form.getComboUser());
        currentEntity.setLackUser(form.getLackUser());
        onSave(currentEntity);
    }

    @Override
    public void onCancel() {
        if (callback != null)
            callback.onCancel();
    }

    public void bind(CustomerOrder entity, EntityDialogCallback<CustomerOrder> callback) {
        currentEntity = entity;
        form.setOrderName(entity.getName());
        form.setComboUser(entity.getComboUser());
        form.setGeneralUser(entity.getGeneralUser());
        form.setLackUser(entity.getLackUser());
        this.callback = callback;
    }

    @Override
    public void onShow() {
        form.setGeneralUsers(userService.getGeneral());
        form.setComboUsers(userService.getCombos());
        form.setLackUsers(userService.getGeneral());
    }

    private void onSave(CustomerOrder currentEntity) {
        if (callback != null)
            callback.onSave(currentEntity);
    }

    private class OrderForm extends FormPanel {
        private final ComboBox<User> generalUsers;
        private final ComboBox<User> comboUsers;
        private final ComboBox<User> lackUsers;

        TextField orderName;


        public void setGeneralUsers(List<User> list) {
            generalUsers.setItems(FXCollections.observableArrayList(list));
        }

        public void setComboUsers(List<User> list) {
            comboUsers.setItems(FXCollections.observableArrayList(list));
        }

        public void setLackUsers(List<User> list) {
            lackUsers.setItems(FXCollections.observableArrayList(list));
        }

        public String getOrderName() {
            return orderName.getText();
        }


        public OrderForm() {
            addRow("Name", orderName = new TextField());
            orderName.setPrefColumnCount(20);

            addRow("General User", generalUsers = new ComboBox<>());
            generalUsers.setId("uneditable-combobox");
            generalUsers.setPromptText("Select user");
            generalUsers.setPrefWidth(200);

            addRow("Combo User", comboUsers = new ComboBox<>());
            comboUsers.setId("uneditable-combobox");
            comboUsers.setPromptText("Select user");
            comboUsers.setPrefWidth(200);

            addRow("Lack User", lackUsers = new ComboBox<>());
            lackUsers.setId("uneditable-combobox");
            lackUsers.setPromptText("Select user");
            lackUsers.setPrefWidth(200);

            orderName.textProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    okBtn.setDisable(
                            orderName.getText() == null || orderName.getText().isEmpty()
                    );
                }
            });

        }

        public User getComboUser() {
            return comboUsers.getSelectionModel().getSelectedItem();
        }

        public User getGeneralUser() {
            return generalUsers.getSelectionModel().getSelectedItem();
        }

        public User getLackUser() {
            return lackUsers.getSelectionModel().getSelectedItem();
        }

        public void setOrderName(String orderName) {
            this.orderName.setText(orderName);
        }

        public void setComboUser(User user) {
            this.comboUsers.getSelectionModel().select(user);
        }

        public void setGeneralUser(User user) {
            this.generalUsers.getSelectionModel().select(user);
        }

        public void setLackUser(User user){
            this.lackUsers.getSelectionModel().select(user);
        }


    }

}
