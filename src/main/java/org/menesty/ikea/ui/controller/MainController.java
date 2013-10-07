package org.menesty.ikea.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.menesty.ikea.ui.ControlledScreen;
import org.menesty.ikea.ui.ScreensController;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, ControlledScreen {
    private ScreensController screensController;
    @Override
    public void setScreenParent(ScreensController screensController) {
              this.screensController = screensController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void goToOrders(ActionEvent event){
        screensController.setScreen("orders");
    }

    @FXML
    private void goToProducts(ActionEvent event){

    }

}
