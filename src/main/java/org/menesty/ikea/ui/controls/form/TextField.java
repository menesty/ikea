package org.menesty.ikea.ui.controls.form;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import org.apache.commons.lang.StringUtils;

public class TextField extends javafx.scene.control.TextField {

    private Timeline delayTimeLine;

    private EventHandler<ActionEvent> delayAction;

    private InvalidationListener invalidationListener;

    private boolean allowBlank = true;

    private String label;

    public TextField() {
        super();
    }

    public TextField(String s, String label) {
        super(s);
        this.label = label;
    }

    public void setDelay(int sec) {
        delayTimeLine = new Timeline(new KeyFrame(Duration.seconds(sec), new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                if (delayAction != null) {
                    delayTimeLine.stop();
                    delayAction.handle(actionEvent);
                }
            }
        }));

        if (invalidationListener != null)
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

    public boolean isValid() {
        boolean result = true;
        getStyleClass().removeAll("validation-succeed", "validation-error");

        if (!allowBlank) {
            result = StringUtils.isNotBlank(getText());
            getStyleClass().remove("white-border");

            if (result)
                getStyleClass().add("validation-succeed");
            else
                getStyleClass().add("validation-error");
        }

        return result;
    }

    public void setAllowBlank(boolean allowBlank) {
        this.allowBlank = allowBlank;

        if (!allowBlank)
            focusedProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (!isFocused())
                        isValid();
                }
            });
    }

    public void setOnDelayAction(EventHandler<ActionEvent> onDelayAction) {
        this.delayAction = onDelayAction;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void reset() {
        setText(null);

        getStyleClass().removeAll("validation-succeed", "validation-error");
        getStyleClass().add("white-border");
    }
}
