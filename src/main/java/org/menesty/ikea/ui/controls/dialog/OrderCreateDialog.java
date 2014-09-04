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
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.form.NumberTextField;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.layout.RowPanel;
import org.menesty.ikea.util.FileChooserUtil;

import java.io.File;
import java.math.BigDecimal;

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
        onCreate(orderForm.getOrderName(), orderForm.getMargin(),
                orderForm.isSynthetic() ? null : orderForm.getFilePath());
    }

    public void onCreate(String orderName, int margin, String filePath) {

    }

    private class OrderForm extends FormPane {
        TextField orderName;
        TextField textField;
        CheckBox synthetic;
        NumberTextField marginField;

        RowPanel filePanel = new RowPanel();

        public String getOrderName() {
            return orderName.getText();
        }

        public String getFilePath() {
            return textField.getText();
        }

        public int getMargin() {
            return marginField.getNumber().intValue();
        }

        public boolean isSynthetic() {
            return synthetic.isSelected();
        }

        public OrderForm() {
            setShowLabels(true);
            setLabelWidth(60);
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

            add(orderName = new TextField(null, "Name", false));
            orderName.setPrefColumnCount(20);

            add(marginField = new NumberTextField(BigDecimal.valueOf(2), "Margin", false));
            marginField.setAllowDouble(false);

            int rowIndex = filePanel.nextRow();
            Label label;
            GridPane.setConstraints(label = new Label("File"), 0, rowIndex);
            label.setPrefWidth(60);


            textField = new TextField();
            textField.setEditable(false);
            GridPane.setConstraints(textField, 1, rowIndex, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);

            Button button = new Button("Browse...");
            button.setId("browseButton");
            button.setMinWidth(USE_PREF_SIZE);
            GridPane.setConstraints(button, 2, rowIndex);

            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    File selectedFile = FileChooserUtil.getXls().showOpenDialog(owner);

                    okBtn.setDisable(selectedFile == null);

                    if (selectedFile != null)
                        textField.setText(selectedFile.getAbsolutePath());
                }
            });

            filePanel.getChildren().addAll(label, textField, button);

            addRow(filePanel, 2);

            orderName.textProperty().addListener(textListener);
            textField.textProperty().addListener(textListener);

        }

    }

}
