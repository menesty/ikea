package org.menesty.ikea.ui.controls.pane;

import javafx.beans.InvalidationListener;
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
    private final int BIG_INDICATOR = 150;
    private final int SMALL_INDICATOR = 50;
    private Region maskRegion;

    private ProgressIndicator progressIndicator;

    private TaskRunningMonitorProperty taskRunningMonitorProperty;
    private SimpleDoubleProperty progressProperty = new SimpleDoubleProperty(-1);

    public LoadingPane() {
        maskRegion = new Region();
        maskRegion.setVisible(false);
        maskRegion.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4)");
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(BIG_INDICATOR, BIG_INDICATOR);
        progressIndicator.setVisible(false);
        getChildren().addAll(maskRegion, progressIndicator);
        setVisible(false);

        taskRunningMonitorProperty = new TaskRunningMonitorProperty();
    }

    public void smallIndicator() {
        progressIndicator.setMaxSize(SMALL_INDICATOR, SMALL_INDICATOR);
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

        List<ReadOnlyBooleanProperty> workerRunningProperties = new ArrayList<>();

        for (Worker<?> worker : task) {
            workerRunningProperties.add(worker.runningProperty());
        }

        taskRunningMonitorProperty.setProperties(workerRunningProperties.toArray(new ReadOnlyBooleanProperty[workerRunningProperties.size()]));

        progressIndicator.progressProperty().bind(progressProperty);
        maskRegion.visibleProperty().bind(taskRunningMonitorProperty);
        visibleProperty().bind(taskRunningMonitorProperty);
        progressIndicator.visibleProperty().bind(taskRunningMonitorProperty);
    }

    public void addBindTask(Worker<?> worker) {
        taskRunningMonitorProperty.addProperty(worker.runningProperty());
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
        invalidationListener = observable -> {
            boolean running = false;
            for (ReadOnlyBooleanProperty property : properties) {
                if (property.get()) {
                    running = true;
                    break;
                }
            }
            TaskRunningMonitorProperty.this.set(running);
        };
    }

    public void setProperties(ReadOnlyBooleanProperty... properties) {
        unbind();

        for (ReadOnlyBooleanProperty property : properties) {
            property.addListener(invalidationListener);
            this.properties.add(property);
        }
    }

    public void addProperty(ReadOnlyBooleanProperty property) {
        property.addListener(invalidationListener);
        this.properties.add(property);
        invalidationListener.invalidated(null);
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
