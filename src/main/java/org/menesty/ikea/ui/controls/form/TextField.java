package org.menesty.ikea.ui.controls.form;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

public class TextField extends HBox implements Field {

    private Timeline delayTimeLine;

    private EventHandler<ActionEvent> delayAction;

    private InvalidationListener invalidationListener;

    private boolean allowBlank = true;

    private String label;

    private Pattern validationPattern;

    protected javafx.scene.control.TextField textField;
    private Label charCountLabel;

    public TextField() {
        textField = new javafx.scene.control.TextField();
        charCountLabel = new Label();
        charCountLabel.setMinWidth(0);
        HBox.setHgrow(textField, Priority.ALWAYS);
        HBox.setMargin(charCountLabel, new Insets(0, 0, 0, 2));
        charCountLabel.setVisible(false);

        textField.textProperty().addListener(observable -> {
            if (charCountLabel.isVisible()) {
                int charCount = textField.getText() != null ? textField.getText().length() : 0;
                charCountLabel.setText(charCount != 0 ? charCount + "" : "");
            }
        });

        getChildren().addAll(textField, charCountLabel);
        textField.heightProperty().addListener((observable, oldValue, newValue) -> charCountLabel.setMinHeight(newValue.doubleValue()));
    }

    public void showCharCounter(boolean showCharCounter) {
        charCountLabel.setVisible(showCharCounter);
    }

    public String getText() {
        return textField.getText();
    }

    public IndexRange getSelection() {
        return textField.getSelection();
    }

    public void setText(String value) {
        textField.setText(value);
    }

    public TextField(String value, String label, boolean allowBlank) {
        this();
        textField.setText(value);
        this.label = label;
        this.allowBlank = allowBlank;
    }

    public TextField(String value, String label) {
        this(value, label, true);
    }

    public void setDelay(int sec) {
        delayTimeLine = new Timeline(new KeyFrame(Duration.seconds(sec), actionEvent -> {
            if (delayAction != null) {
                delayTimeLine.stop();
                delayAction.handle(actionEvent);
            }
        }));

        if (invalidationListener != null)
            textField.textProperty().removeListener(invalidationListener);

        invalidationListener = observable -> {
            delayTimeLine.stop();
            delayTimeLine.playFromStart();
        };
        textField.textProperty().addListener(invalidationListener);
    }

    @Override
    public boolean isValid() {
        boolean result = true;

        if (!allowBlank) {
            result = StringUtils.isNotBlank(textField.getText());
        }

        if (validationPattern != null && textField.getText() != null) {
            result = validationPattern.matcher(textField.getText()).find();
        }

        return result;
    }

    @Override
    public void setValid(boolean valid) {
        textField.getStyleClass().removeAll("validation-succeed", "validation-error");
        textField.getStyleClass().remove("white-border");

        if (valid)
            textField.getStyleClass().add("validation-succeed");
        else
            textField.getStyleClass().add("validation-error");

    }

    public void setAllowBlank(boolean allowBlank) {
        this.allowBlank = allowBlank;

        if (!allowBlank)
            focusedProperty().addListener(observable -> {
                if (!isFocused())
                    isValid();
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

    @Override
    public void reset() {
        textField.setText(null);

        textField.getStyleClass().removeAll("validation-succeed", "validation-error");
        textField.getStyleClass().addAll("text-input", "text-field");
    }

    public void setValidationPattern(Pattern validationPattern) {
        this.validationPattern = validationPattern;
    }

    public void setPromptText(String promptText) {
        textField.setPromptText(promptText);
    }

    public StringProperty textProperty() {
        return textField.textProperty();
    }

    public boolean isEditable() {
        return textField.isEditable();
    }

    public void setPrefColumnCount(int prefColumnCount) {
        textField.setPrefColumnCount(prefColumnCount);
    }

    public void setEditable(boolean editable) {
        textField.setEditable(editable);
    }

    public void setOnAction(EventHandler<ActionEvent> value) {
        textField.setOnAction(value);
    }

    public void selectAll() {
        textField.selectAll();
    }

    public void clear() {
        textField.clear();
    }
}
