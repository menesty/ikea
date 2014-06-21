package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import org.menesty.ikea.db.DatabaseService;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.IkeaShop;
import org.menesty.ikea.domain.User;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.BaseEntityDialog;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.form.ListEditField;
import org.menesty.ikea.ui.controls.form.TextField;

import java.util.List;
import java.util.concurrent.Callable;

public class OrderEditDialog extends BaseEntityDialog<CustomerOrder> {
    private OrderForm form;

    public OrderEditDialog() {
        okBtn.setDisable(true);
        setTitle("Edit order");

        form = new OrderForm();

        addRow(form, bottomBar);
    }

    @Override
    protected CustomerOrder collect() {
        entityValue.setName(form.getOrderName());
        entityValue.setUsers(form.getUsers());
        entityValue.setIkeaShops(form.getShops());
        entityValue.setLackUser(form.getLackUser());
        return entityValue;
    }

    @Override
    protected void populate(CustomerOrder entityValue) {
        form.setOrderName(entityValue.getName());
        form.setUsers(entityValue.getUsers());
        form.setShops(entityValue.getIkeaShops());
        form.setLackUser(entityValue.getLackUser());

        DatabaseService.runInTransaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                form.setChoiceUsers(ServiceFacade.getUserService().load(false));
                form.setChoiceShops(ServiceFacade.getIkeaShopService().load());
                form.setLackUsers(ServiceFacade.getUserService().load(false));
                return null;
            }
        });
    }

    @Override
    public boolean isValid() {
        return form.isValid();
    }

    @Override
    public void reset() {
        form.reset();
    }

    private class OrderForm extends FormPane {
        private final ListEditField<User> userListView;
        private final ListEditField<IkeaShop> ikeaShopView;

        private final ComboBox<User> lackUsers;

        TextField orderName;

        public void setChoiceUsers(List<User> list) {
            userListView.setChoiceList(list);
        }

        public void setChoiceShops(List<IkeaShop> shops) {
            ikeaShopView.setChoiceList(shops);
        }

        public void setLackUsers(List<User> list) {
            lackUsers.setItems(FXCollections.observableArrayList(list));
        }

        public void setUsers(List<User> users) {
            userListView.setValue(users);
        }

        public void setShops(List<IkeaShop> shops) {
            ikeaShopView.setValue(shops);
        }

        public List<User> getUsers() {
            return userListView.getValues();
        }

        public List<IkeaShop> getShops() {
            return ikeaShopView.getValues();
        }

        public String getOrderName() {
            return orderName.getText();
        }

        public OrderForm() {
            add(orderName = new TextField(null, "Name", false));
            orderName.setPrefColumnCount(20);

            add(userListView = new ListEditField<User>("Users", false) {
                @Override
                public boolean isValid() {
                    boolean result = super.isValid();

                    if (result)
                        super.setValid(result = this.getValues().size() >= ikeaShopView.getValues().size());

                    return result;
                }
            }, 2);
            userListView.setMaxHeight(120);

            add(ikeaShopView = new ListEditField<>("Shops", false), 2);
            ikeaShopView.setMaxHeight(120);

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

        public User getLackUser() {
            return lackUsers.getSelectionModel().getSelectedItem();
        }

        public void setOrderName(String orderName) {
            this.orderName.setText(orderName);
        }

        public void setLackUser(User user) {
            this.lackUsers.getSelectionModel().select(user);
        }

    }

}
