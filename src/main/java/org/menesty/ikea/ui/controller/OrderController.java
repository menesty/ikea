package org.menesty.ikea.ui.controller;

import javafx.fxml.Initializable;
import org.menesty.ikea.ui.ControlledScreen;
import org.menesty.ikea.ui.ScreensController;

import java.net.URL;
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

}
