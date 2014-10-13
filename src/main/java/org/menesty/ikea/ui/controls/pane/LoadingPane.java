package org.menesty.ikea.ui.controls.pane;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Worker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty
 * on 12/18/13.
 */
public class LoadingPane extends StackPane {

    private Region maskRegion;

    private ProgressIndicator progressIndicator;

    private TaskRunningMonitorProperty taskRunningMonitorProperty;
    private SimpleDoubleProperty progressProperty = new SimpleDoubleProperty(-1);

    public LoadingPane() {
        maskRegion = new Region();
        maskRegion.setVisible(false);
        maskRegion.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4)");
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(150, 150);
        progressIndicator.setVisible(false);
        getChildren().addAll(maskRegion, progressIndicator);
        setVisible(false);

        taskRunningMonitorProperty = new TaskRunningMonitorProperty();
    }

    private void unbind() {
        progressIndicator.progressProperty().unbind();
        maskRegion.visibleProperty().unbind();
        visibleProperty().unbind();
        progressIndicator.visibleProperty().unbind();
        taskRunningMonitorProperty.unbind();
    }

    public void bindTask(Worker<?>... task) {
        unbind();

        for (Worker<?> worker : task) {
            taskRunningMonitorProperty.setProperties(worker.runningProperty());
        }

        progressIndicator.progressProperty().bind(progressProperty);
        maskRegion.visibleProperty().bind(taskRunningMonitorProperty);
        visibleProperty().bind(taskRunningMonitorProperty);
        progressIndicator.visibleProperty().bind(taskRunningMonitorProperty);
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

class TaskRunningMonitorProperty extends SimpleBooleanProperty {
    private List<ReadOnlyBooleanProperty> properties = new ArrayList<>();

    private InvalidationListener invalidationListener;

    public TaskRunningMonitorProperty() {
        invalidationListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                boolean running = false;
                for (ReadOnlyBooleanProperty property : properties) {
                    if (property.get()) {
                        running = true;
                        break;
                    }
                }
                TaskRunningMonitorProperty.this.set(running);
            }
        };
    }

    public void setProperties(ReadOnlyBooleanProperty... properties) {
        unbind();

        for (ReadOnlyBooleanProperty property : properties) {
            property.addListener(invalidationListener);
            this.properties.add(property);
        }
    }

    @Override
    public void unbind() {
        super.unbind();

        for (ReadOnlyBooleanProperty property : properties) {
            property.removeListener(invalidationListener);
        }

        properties.clear();
    }
}
