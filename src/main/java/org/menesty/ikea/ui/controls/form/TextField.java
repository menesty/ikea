package org.menesty.ikea.ui.controls.form;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public class TextField extends javafx.scene.control.TextField {

    private Timeline delayTimeLine;

    private EventHandler<ActionEvent> delayAction;

    private InvalidationListener invalidationListener;

    public TextField() {
        super();
    }

    public TextField(String s) {
        super(s);
    }

    public void setDelay(int sec) {
        delayTimeLine = new Timeline(new KeyFrame(Duration.seconds(sec), new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                if (delayAction != null)
                    delayAction.handle(actionEvent);
            }
        }));
        textProperty().removeListener(invalidationListener);
        invalidationListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                delayTimeLine.stop();
                delayTimeLine.playFromStart();
            }
        };
        textProperty().addListener(invalidationListener);
    }

    public void setOnDelayAction(EventHandler<ActionEvent> onDelayAction) {
        this.delayAction = onDelayAction;
    }
}
