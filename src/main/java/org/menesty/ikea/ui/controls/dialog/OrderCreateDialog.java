package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.form.NumberTextField;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.layout.RowPanel;
import org.menesty.ikea.util.FileChooserUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class OrderCreateDialog extends BaseDialog {
    public enum OrderType {
        XLS, PDF
    }

    private OrderForm orderForm;


    public OrderCreateDialog(final Stage stage) {
        super(stage);

        okBtn.setDisable(true);
        setTitle("New order from customer");

        orderForm = new OrderForm();

        addRow(orderForm, bottomBar);
    }

    @Override
    public void onOk() {
        if (orderForm.isValid())
            onCreate(orderForm.getOrderName(), orderForm.getMargin(), orderForm.getOrderType(), orderForm.getFilePath());
    }

    public void onCreate(String orderName, int margin, OrderType orderType, String filePath) {

    }

    private class OrderForm extends FormPane {
        TextField orderName;
        TextField textField;
        NumberTextField marginField;
        ComboBox<OrderType> comboBox;

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

        public OrderType getOrderType() {
            return comboBox.getSelectionModel().getSelectedItem();
        }

        public OrderForm() {
            setShowLabels(true);
            setLabelWidth(60);
            filePanel.setPadding(new Insets(0));
            final ChangeListener<String> textListener = (ov, t, t1) ->
                    okBtn.setDisable(orderName.getText() == null || orderName.getText().isEmpty()
                            && (comboBox.getSelectionModel().getSelectedItem() != null && textField.getText() == null || textField.getText().isEmpty()));

            add(orderName = new TextField(null, "Name", false));
            orderName.setPrefColumnCount(20);

            add(marginField = new NumberTextField(BigDecimal.valueOf(2), "Margin", false));
            marginField.setAllowDouble(false);

            int rowIndex = filePanel.nextRow();
            Label label;
            comboBox = new ComboBox<>();
            comboBox.getItems().addAll(null, OrderType.XLS, OrderType.PDF);
            GridPane.setConstraints(label = new Label("Order Type"), 0, rowIndex);
            GridPane.setConstraints(comboBox, 1, rowIndex, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
            filePanel.getChildren().addAll(label, comboBox);

            rowIndex = filePanel.nextRow();

            GridPane.setConstraints(label = new Label("File"), 0, rowIndex);
            label.setPrefWidth(60);

            textField = new TextField();
            textField.setEditable(false);
            GridPane.setConstraints(textField, 1, rowIndex, 1, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);

            Button button = new Button("Browse...");
            button.setId("browseButton");
            button.setMinWidth(USE_PREF_SIZE);
            GridPane.setConstraints(button, 2, rowIndex);

            button.setOnAction(actionEvent -> {
                OrderType selected = comboBox.getSelectionModel().getSelectedItem();

                if (selected != null) {
                    if (OrderType.XLS == selected) {
                        File selectedFile = FileChooserUtil.getXls().showOpenDialog(getStage());

                        okBtn.setDisable(selectedFile == null);

                        if (selectedFile != null) {
                            textField.setText(selectedFile.getAbsolutePath());
                        }
                    } else if (OrderType.PDF == selected) {
                        List<File> selectedFiles = FileChooserUtil.getPdf().showOpenMultipleDialog(getStage());

                        if (!selectedFiles.isEmpty()) {
                            String items = selectedFiles.stream().map(File::getAbsolutePath).collect(Collectors.joining(";"));

                            textField.setText(items);
                        }
                    }
                }
            });

            filePanel.getChildren().addAll(label, textField, button);

            addRow(filePanel, 2);

            orderName.textProperty().addListener(textListener);
            textField.textProperty().addListener(textListener);

        }

    }

}
