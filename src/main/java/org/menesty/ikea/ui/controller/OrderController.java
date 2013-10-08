package org.menesty.ikea.ui.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.lang.StringUtils;
import org.menesty.ikea.MainApp;
import org.menesty.ikea.domain.Order;
import org.menesty.ikea.ui.ControlledScreen;
import org.menesty.ikea.ui.Dialogs;
import org.menesty.ikea.ui.ScreensController;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class OrderController implements Initializable, ControlledScreen {

    ScreensController screensController;


    @Override
    public void setScreenParent(ScreensController screensController) {
        this.screensController = screensController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


    public void createOrder(ActionEvent event) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 10, 0, 10));
        final TextField orderName = new TextField();
        orderName.setPromptText("Order name");

        final Label labelFile = new Label();
        final OrderCreateState orderState = new OrderCreateState();
        Button btn = new Button();
        btn.setText("choose");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Exel files (*.xls,*.xlsx)", Arrays.asList("*.xls", "*.xlsx"));
                fileChooser.getExtensionFilters().add(extFilter);

                //Show open file dialog
                orderState.xlsFile = fileChooser.showOpenDialog(null);
                if (orderState.xlsFile != null)
                    labelFile.setText(orderState.xlsFile.getName());
            }
        });


        grid.add(new Label("Name:"), 0, 0);
        grid.add(orderName, 1, 0);
        grid.add(new Label("File:"), 0, 1);
        grid.add(labelFile, 1, 1);
        grid.add(btn, 2, 1);


        Callback<Dialogs.DialogResponse, Boolean> createCallback = new Callback<Dialogs.DialogResponse, Boolean>() {
            @Override
            public Boolean call(Dialogs.DialogResponse param) {
                if (Dialogs.DialogResponse.OK == param) {
                    orderState.name = orderName.getText();
                    return orderState.isValid();
                }
                return true;
            }
        };

        Dialogs.DialogResponse resp = Dialogs.showCustomDialog(MainApp.STAGE, grid, "Order information", "Create order", Dialogs.DialogOptions.OK_CANCEL, createCallback);
        System.out.println("Custom Dialog: User clicked: " + resp);

        if(orderState.isValid()) {
            Order order = new Order();
            order.setName(orderState.name);
        }

    }

    class OrderCreateState {
        public String name;

        public File xlsFile;

        public boolean isValid() {
            return StringUtils.isNotBlank(name) && xlsFile != null;
        }
    }
}
