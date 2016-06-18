package org.menesty.ikea.core.component.ui;

import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.menesty.ikea.ui.controls.WindowResizeButton;

/**
 * Created by Menesty on
 * 10/11/14.
 * 10:16.
 */
public class ApplicationWindow extends StackPane {
    private final WindowResizeButton windowResizeButton;

    public ApplicationWindow(Stage stage, int minWeight, int minHeight) {
        windowResizeButton = new WindowResizeButton(stage, minWeight, minHeight);

        getStyleClass().addAll("stagePane");
        setDepthTest(DepthTest.DISABLE);
        getChildren().addAll(windowResizeButton);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        windowResizeButton.autosize();
        windowResizeButton.setLayoutX(getWidth() - windowResizeButton.getLayoutBounds().getWidth());
        windowResizeButton.setLayoutY(getHeight() - windowResizeButton.getLayoutBounds().getHeight());
    }

    public void addFirst(Node node) {
        getChildren().add(0, node);
    }

    public void add(Node node) {
        getChildren().add(node);
    }
}
