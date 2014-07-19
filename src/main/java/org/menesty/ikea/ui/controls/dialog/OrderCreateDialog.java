package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.menesty.ikea.ui.layout.RowPanel;
import org.menesty.ikea.util.FileChooserUtil;

import java.io.File;
import java.util.Arrays;

public class OrderCreateDialog extends BaseDialog {

    private OrderForm orderForm;

    private final Window owner;

    public OrderCreateDialog(final Window owner) {
        this.owner = owner;

        okBtn.setDisable(true);
        setTitle("New order from customer");

        orderForm = new OrderForm();

        addRow(orderForm, bottomBar);
    }

    @Override
    public void onOk() {
        onCreate(orderForm.getOrderName(), orderForm.getFilePath());
    }

    public void onCreate(String orderName, String filePath) {

    }

    private class OrderForm extends RowPanel {
        TextField orderName;
        TextField textField;

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
                    File selectedFile = FileChooserUtil.getXls().showOpenDialog(owner);

                    okBtn.setDisable(selectedFile == null);

                    if (selectedFile != null)
                        textField.setText(selectedFile.getAbsolutePath());
                }
            });
            getChildren().addAll(textField, button);

            ChangeListener<String> textListener = new ChangeListener<String>() {
                public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                    okBtn.setDisable(orderName.getText() == null || orderName.getText().isEmpty()
                                    || textField.getText() == null || textField.getText().isEmpty());
                }
            };

            orderName.textProperty().addListener(textListener);
            textField.textProperty().addListener(textListener);

        }

    }

}
