package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.menesty.ikea.domain.User;
import org.menesty.ikea.service.UserService;
import org.menesty.ikea.ui.layout.RowPanel;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class OrderCreateDialog extends BaseDialog {

    private OrderForm orderForm;

    private final Window owner;

    private UserService userService;


    public OrderCreateDialog(final Window owner) {
        this.owner = owner;
        userService = new UserService();

        okBtn.setDisable(true);
        setTitle("Create new order from customer");

        orderForm = new OrderForm();

        addRow(orderForm, bottomBar);
    }

    @Override
    public void onOk() {
        onCreate(orderForm.getOrderName(), orderForm.getFilePath());
    }

    @Override
    public void onShow() {
        orderForm.setGeneralUsers(userService.load(false));
        orderForm.setComboUsers(userService.load(true));
    }

    public void onCreate(String orderName, String filePath) {

    }

    private class OrderForm extends RowPanel {
        private final ComboBox<User> generalUsers;
        private final ComboBox<User> comboUsers;
        TextField orderName;
        TextField textField;


        public void setGeneralUsers(List<User> list) {
            generalUsers.setItems(FXCollections.observableArrayList(list));
        }

        public void setComboUsers(List<User> list) {
            comboUsers.setItems(FXCollections.observableArrayList(list));
        }

        public String getOrderName() {
            return orderName.getText();
        }

        public String getFilePath() {
            return textField.getText();
        }

        public OrderForm() {
            addRow("Name");
            orderName = new TextField();
            orderName.setPrefColumnCount(20);
            addRow(orderName);

            addRow("General User");
            generalUsers = new ComboBox<>();
            generalUsers.setId("uneditable-combobox");
            generalUsers.setPrefWidth(200);
            generalUsers.setPromptText("Select user");
            addRow(generalUsers);

            addRow("Combo User");
            comboUsers = new ComboBox<>();
            comboUsers.setPrefWidth(200);
            comboUsers.setId("uneditable-combobox");
            comboUsers.setPromptText("Select user");
            addRow(comboUsers);


            addRow("File");

            int rowIndex = nextRow();
            textField = new TextField();
            textField.setEditable(false);
            GridPane.setConstraints(textField, 0, rowIndex, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
            Button button = new Button("Browse...");
            button.setId("browseButton");
            button.setMinWidth(USE_PREF_SIZE);
            GridPane.setConstraints(button, 1, rowIndex);
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("CustomerOrder Exel location");
                    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Exel files (*.xls,*.xlsx)", Arrays.asList("*.xls", "*.xlsx"));
                    fileChooser.getExtensionFilters().add(extFilter);
                    File selectedFile = fileChooser.showOpenDialog(owner);

                    okBtn.setDisable(selectedFile == null);
                    if (selectedFile != null)
                        textField.setText(selectedFile.getAbsolutePath());
                }
            });
            getChildren().addAll(textField, button);


            ChangeListener<String> textListener = new ChangeListener<String>() {
                public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                    okBtn.setDisable(
                            orderName.getText() == null || orderName.getText().isEmpty()
                                    || textField.getText() == null || textField.getText().isEmpty());
                }
            };
            orderName.textProperty().addListener(textListener);
            textField.textProperty().addListener(textListener);

        }

    }

}
