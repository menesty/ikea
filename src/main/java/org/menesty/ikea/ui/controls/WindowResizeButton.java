package org.menesty.ikea.ui.controls;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * User: Menesty
 * Date: 10/9/13
 * Time: 6:19 PM
 */
public class WindowResizeButton extends Region {
    private double dragOffsetX, dragOffsetY;

    public WindowResizeButton(final Stage stage, final double stageMinimumWidth, final double stageMinimumHeight) {
        setId("window-resize-button");
        setPrefSize(11, 11);
        setOnMousePressed(e -> {
            dragOffsetX = (stage.getX() + stage.getWidth()) - e.getScreenX();
            dragOffsetY = (stage.getY() + stage.getHeight()) - e.getScreenY();
            e.consume();
        });

        setOnMouseDragged(e -> {
            ObservableList<Screen> screens = Screen.getScreensForRectangle(stage.getX(), stage.getY(), 1, 1);
            final Screen screen;

            if (screens.size() > 0)
                screen = screens.get(0);
            else
                screen = Screen.getScreensForRectangle(0, 0, 1, 1).get(0);

            Rectangle2D visualBounds = screen.getVisualBounds();
            double maxX = Math.min(visualBounds.getMaxX(), e.getScreenX() + dragOffsetX);
            double maxY = Math.min(visualBounds.getMaxY(), e.getScreenY() - dragOffsetY);
            stage.setWidth(Math.max(stageMinimumWidth, maxX - stage.getX()));
            stage.setHeight(Math.max(stageMinimumHeight, maxY - stage.getY()));
            e.consume();
        });

        setManaged(false);
    }
}
