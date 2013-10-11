package org.menesty.ikea.ui.pages;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Arrays;

public class OrderCreateDialog extends VBox {
    private Button okBtn;
    private final Window owner;

    private OrderForm orderForm;

    public OrderCreateDialog(final Window owner) {
        this.owner = owner;
        setId("ProxyDialog");
        setSpacing(10);
        setMaxSize(430, USE_PREF_SIZE);
        // block mouse clicks
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent t) {
                t.consume();
            }
        });


        // create title
        Label title = new Label("Create new order from customer");
        title.setId("title");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        getChildren().add(title);


        orderForm = new OrderForm();

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setId("cancelButton");
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                onCancel();
            }
        });
        cancelBtn.setMinWidth(74);
        cancelBtn.setPrefWidth(74);
        HBox.setMargin(cancelBtn, new Insets(0, 8, 0, 0));
        okBtn = new Button("Create");
        okBtn.setId("saveButton");
        okBtn.setDefaultButton(true);
        okBtn.setDisable(true);
        okBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                onCreate(orderForm.getOrderName(), orderForm.getFilePath());
            }
        });
        okBtn.setMinWidth(74);
        okBtn.setPrefWidth(74);

        HBox bottomBar = new HBox(0);
        bottomBar.setAlignment(Pos.BASELINE_RIGHT);
        bottomBar.getChildren().addAll(cancelBtn, okBtn);
        VBox.setMargin(bottomBar, new Insets(20, 5, 5, 5));

        getChildren().addAll(orderForm, bottomBar);
    }

    public void onCancel() {

    }

    public void onCreate(String orderName, String filePath) {

    }

    private class OrderForm extends GridPane {
        TextField orderName;
        TextField textField;

        public String getOrderName() {
            return orderName.getText();
        }

        public String getFilePath() {
            return textField.getText();
        }

        public OrderForm() {
            setPadding(new Insets(8));
            setHgap(5.0F);
            setVgap(5.0F);

            int rowIndex = 0;

            Label label2 = new Label("Name");
            label2.setId("proxy-dialog-label");
            GridPane.setConstraints(label2, 0, rowIndex);

            rowIndex++;
            orderName = new TextField();
            orderName.setPrefColumnCount(20);
            GridPane.setConstraints(orderName, 0, rowIndex);
            getChildren().addAll(label2, orderName);

            rowIndex++;
            Label label3 = new Label("File");
            label3.setId("proxy-dialog-label");
            GridPane.setConstraints(label3, 0, rowIndex);

            rowIndex++;
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
                    fileChooser.setTitle("Order Exel location");
                    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Exel files (*.xls,*.xlsx)", Arrays.asList("*.xls", "*.xlsx"));
                    fileChooser.getExtensionFilters().add(extFilter);
                    File selectedFile = fileChooser.showOpenDialog(owner);

                    okBtn.setDisable(selectedFile == null);
                    if (selectedFile != null)
                        textField.setText(selectedFile.getAbsolutePath());
                }
            });
            getChildren().addAll(label3, textField, button);


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
