package org.menesty.ikea.ui.controls;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * User: Menesty
 * Date: 10/9/13
 * Time: 7:03 PM
 */
public class WindowButtons extends VBox {
    private Stage stage;
    private Rectangle2D backupWindowBounds = null;
    private boolean maximized = false;

    public WindowButtons(final Stage stage) {
        super(4);
        this.stage = stage;
        // create buttons
        Button closeBtn = new Button();
        closeBtn.setId("window-close");
        closeBtn.setOnAction(actionEvent -> Platform.exit());

        Button minBtn = new Button();
        minBtn.setId("window-min");
        minBtn.setOnAction(actionEvent -> stage.setIconified(true));

        Button maxBtn = new Button();
        maxBtn.setId("window-max");
        maxBtn.setOnAction(actionEvent -> toggleMaximized());

        getChildren().addAll(closeBtn, minBtn, maxBtn);
    }

    public void toggleMaximized() {
        final Screen screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()).get(0);
        if (maximized) {
            maximized = false;
            if (backupWindowBounds != null) {
                stage.setX(backupWindowBounds.getMinX());
                stage.setY(backupWindowBounds.getMinY());
                stage.setWidth(backupWindowBounds.getWidth());
                stage.setHeight(backupWindowBounds.getHeight());
            }
        } else {
            maximized = true;
            backupWindowBounds = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            stage.setX(screen.getVisualBounds().getMinX());
            if (screen.getBounds().getMinY() != screen.getVisualBounds().getMinY()) {
                stage.setY(screen.getBounds().getMinY() + screen.getVisualBounds().getMinY());
            } else {
                stage.setY(screen.getVisualBounds().getMinY());
            }
            stage.setWidth(screen.getVisualBounds().getWidth());
            stage.setHeight(screen.getVisualBounds().getHeight());
        }
    }

    public boolean isMaximized() {
        return maximized;
    }
}