package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.layout.RowPanel;
import org.menesty.ikea.util.FileChooserUtil;

import java.io.File;

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
        onCreate(orderForm.getOrderName(), orderForm.isSynthetic() ? null : orderForm.getFilePath());
    }

    public void onCreate(String orderName, String filePath) {

    }

    private class OrderForm extends FormPane {
        TextField orderName;
        TextField textField;
        CheckBox synthetic;
        RowPanel filePanel = new RowPanel();

        public String getOrderName() {
            return orderName.getText();
        }

        public String getFilePath() {
            return textField.getText();
        }

        public boolean isSynthetic() {
            return synthetic.isSelected();
        }

        public OrderForm() {
            setShowLabels(false);
            filePanel.setPadding(new Insets(0));
            final ChangeListener<String> textListener = new ChangeListener<String>() {
                public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                    if (!synthetic.isSelected())
                        okBtn.setDisable(orderName.getText() == null || orderName.getText().isEmpty()
                                || textField.getText() == null || textField.getText().isEmpty());
                }
            };

            addRow("Synthetic", synthetic = new CheckBox());
            synthetic.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                    filePanel.setDisable(synthetic.isSelected());

                    if (synthetic.isSelected())
                        okBtn.setDisable(false);
                    else
                        textListener.changed(null, null, null);
                }
            });

            addRow("Name");
            orderName = new TextField();
            orderName.setAllowBlank(false);
            orderName.setPrefColumnCount(20);
            add(orderName);

            filePanel.addRow("File");

            int rowIndex = filePanel.nextRow();
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
            filePanel.getChildren().addAll(textField, button);

            addRow(filePanel);


            orderName.textProperty().addListener(textListener);
            textField.textProperty().addListener(textListener);

        }

    }

}
