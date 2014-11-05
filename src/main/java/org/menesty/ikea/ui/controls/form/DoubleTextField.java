package org.menesty.ikea.ui.controls.form;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang.math.NumberUtils;
import org.menesty.ikea.util.NumberUtil;

/**
 * User: Menesty
 * Date: 10/14/13
 * Time: 11:51 PM
 */
public class DoubleTextField extends TextField {

    private SimpleDoubleProperty number = new SimpleDoubleProperty(0);

    public final Double getNumber() {
        return number.get();
    }

    public final void setNumber(Double value) {
        number.set(value);
        setText(value + "");
    }

    public SimpleDoubleProperty numberProperty() {
        return number;
    }

    public DoubleTextField() {
        this(null);
    }

    public DoubleTextField(String label) {
        this(0d, label);
    }

    public DoubleTextField(Double value, String label) {
        super();
        setLabel(label);
        initHandlers();
        setNumber(value);
    }

    private void initHandlers() {

        // try to parse when focus is lost or RETURN is hit
        setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                parseAndFormatInput();
            }
        });

        focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue)
                    parseAndFormatInput();

            }
        });

        addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                String newValue = getText().substring(0, getSelection().getStart()) + event.getCharacter() + getText().substring(getSelection().getEnd(), getText().length());

                if (!NumberUtils.isNumber(newValue))
                    event.consume();
            }
        });

        textProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                parseAndFormatInput();
            }
        });

    }

    /**
     * Tries to parse the user input to a number according to the provided
     * NumberFormat
     */
    private void parseAndFormatInput() {
        String input = getText();

        if (input == null || input.length() == 0) {
            number.setValue(null);
            return;
        }

        number.setValue(NumberUtils.createDouble(input));
        selectAll();
    }

    @Override
    public void reset() {
        super.reset();
        setNumber(0d);
    }
}