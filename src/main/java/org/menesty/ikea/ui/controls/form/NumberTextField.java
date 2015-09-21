package org.menesty.ikea.ui.controls.form;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang.math.NumberUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * User: Menesty
 * Date: 10/14/13
 * Time: 11:51 PM
 */
public class NumberTextField extends TextField {

    private final NumberFormat nf;
    private ObjectProperty<BigDecimal> number = new SimpleObjectProperty<>();

    private boolean allowDouble = true;

    public final BigDecimal getNumber() {
        return number.get();
    }

    public final void setNumber(BigDecimal value) {
        number.set(value);
        updateView();
    }

    public ObjectProperty<BigDecimal> numberProperty() {
        return number;
    }

    public NumberTextField(String label, boolean allowBlank) {
        this(BigDecimal.ZERO, label, allowBlank);
    }

    public NumberTextField(BigDecimal value, String label, boolean allowBlank) {
        this(value, NumberFormat.getInstance(), label, allowBlank);
    }

    public void setAllowDouble(boolean allowDouble) {
        this.allowDouble = allowDouble;
        updateView();
    }

    public NumberTextField(BigDecimal value, NumberFormat nf, String label, boolean allowBlank) {
        super();
        this.nf = nf;
        setAllowBlank(allowBlank);
        setNumber(value);
        setLabel(label);
        initHandlers();
    }

    private void updateView() {
        BigDecimal newValue = getNumber();

        if (newValue != null)
            setText((allowDouble ? newValue.doubleValue() + "" : newValue.intValue() + ""));
        else
            setText("0");
    }

    private void initHandlers() {
        // try to parse when focus is lost or RETURN is hit
        setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                parseAndFormatInput();
            }
        });

        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                parseAndFormatInput();
        });

        // Set text in field if BigDecimal property is changed from outside.
        numberProperty().addListener((obserable, oldValue, newValue) -> {
            updateView();
        });

        addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!allowDouble && event.getCharacter().contains(".")) {
                event.consume();
                return;
            }
            String newValue = "";

            if (getText() != null) {
                newValue = getText().substring(0, getSelection().getStart()) + event.getCharacter() +
                        getText().substring(getSelection().getEnd(), getText().length());
            } else {
                newValue = event.getCharacter();
            }
            if (!NumberUtils.isNumber(newValue))
                event.consume();
        });

    }

    /**
     * Tries to parse the user input to a number according to the provided
     * NumberFormat
     */
    private void parseAndFormatInput() {
        try {
            String input = getText();

            if (input == null || input.length() == 0)
                return;

            Number parsedNumber = nf.parse(input);
            BigDecimal newValue = new BigDecimal(allowDouble ? parsedNumber.doubleValue() : parsedNumber.intValue());
            setNumber(newValue);
            selectAll();
        } catch (ParseException ex) {
            // If parsing fails keep old number
            setText((allowDouble ? number.get().doubleValue() : number.get().intValue()) + "");
        }
    }
}