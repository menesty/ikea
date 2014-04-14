package org.menesty.ikea.ui.controls.pane;

import javafx.concurrent.Worker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Created by Menesty on 12/18/13.
 */
public class LoadingPane extends StackPane {

    private Region maskRegion;

    private ProgressIndicator progressIndicator;

    public LoadingPane() {
        maskRegion = new Region();
        maskRegion.setVisible(false);
        maskRegion.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4)");
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(150, 150);
        progressIndicator.setVisible(false);
        getChildren().addAll(maskRegion, progressIndicator);
        setVisible(false);
    }

    private void unbind() {
        progressIndicator.progressProperty().unbind();
        maskRegion.visibleProperty().unbind();
        visibleProperty().unbind();
        progressIndicator.visibleProperty().unbind();
    }

    public void bindTask(Worker<?> task) {
        unbind();

        progressIndicator.progressProperty().bind(task.progressProperty());
        maskRegion.visibleProperty().bind(task.runningProperty());
        visibleProperty().bind(task.runningProperty());
        progressIndicator.visibleProperty().bind(task.runningProperty());
    }

    public void show() {
        unbind();

        setVisible(true);
        maskRegion.setVisible(true);
        progressIndicator.setVisible(true);
    }

    public void hide() {
        unbind();

        maskRegion.visibleProperty().setValue(false);
        visibleProperty().setValue(false);
        progressIndicator.visibleProperty().setValue(false);
    }
}
