package org.menesty.ikea.ui.controls.form;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang.math.NumberUtils;

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
        setOnAction(arg0 -> parseAndFormatInput());

        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                parseAndFormatInput();

        });

        addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String newValue = getText().substring(0, getSelection().getStart()) + event.getCharacter() + getText().substring(getSelection().getEnd(), getText().length());

            if (!NumberUtils.isNumber(newValue))
                event.consume();
        });

        textProperty().addListener(observable -> {
            parseAndFormatInput();
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