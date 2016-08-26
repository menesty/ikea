package org.menesty.ikea.core.component.ui;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.menesty.ikea.ui.controls.WindowButtons;

/**
 * User: Menesty
 * Date: 10/9/13
 * Time: 6:58 PM
 */
public class ApplicationControlToolBar extends ToolBar {

    private double mouseDragOffsetX = 0;

    private double mouseDragOffsetY = 0;

    public ApplicationControlToolBar(final Stage stage) {
        setId("mainToolBar");
        ImageView logo = new ImageView(new Image(ApplicationControlToolBar.class.getResourceAsStream("/styles/images/logo.png")));
        HBox.setMargin(logo, new Insets(0, 0, 0, 5));
        getItems().add(logo);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getItems().add(spacer);

        setPrefHeight(66);
        setMinHeight(66);
        setMaxHeight(66);
        GridPane.setConstraints(this, 0, 0);

        // add close min max
        final WindowButtons windowButtons = new WindowButtons(stage);
        getItems().add(windowButtons);
        // add window header double clicking
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2)
                windowButtons.toggleMaximized();
        });
        // add window dragging
        setOnMousePressed(event -> {
            mouseDragOffsetX = event.getSceneX();
            mouseDragOffsetY = event.getSceneY();
        });

        setOnMouseDragged(event -> {
            if (!windowButtons.isMaximized()) {
                stage.setX(event.getScreenX() - mouseDragOffsetX);
                stage.setY(event.getScreenY() - mouseDragOffsetY);
            }
        });
    }
}
