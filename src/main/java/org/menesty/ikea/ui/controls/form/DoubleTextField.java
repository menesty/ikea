package org.menesty.ikea.ui.controls.form;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang.math.NumberUtils;
import org.menesty.ikea.util.NumberUtil;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * User: Menesty
 * Date: 10/14/13
 * Time: 11:51 PM
 */
public class DoubleTextField extends TextField {

    private final NumberFormat nf;
    private SimpleDoubleProperty number = new SimpleDoubleProperty(0);

    public final Double getNumber() {
        return number.get();
    }

    public final void setNumber(Double value) {
        number.set(value);
    }

    public SimpleDoubleProperty numberProperty() {
        return number;
    }

    public DoubleTextField() {
        this(0d);
    }


    public DoubleTextField(Double value) {
        this(value, NumberFormat.getInstance());
        initHandlers();
    }

    public DoubleTextField(Double value, NumberFormat nf) {
        super();
        this.nf = nf;
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
                if (!newValue.booleanValue()) {
                    parseAndFormatInput();
                }
            }
        });

        // Set text in field if BigDecimal property is changed from outside.
        numberProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obserable, Number oldValue, Number newValue) {
                setText(newValue.doubleValue() + "");
            }
        });

        addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                String newValue = getText().substring(0, getSelection().getStart()) + event.getCharacter() + getText().substring(getSelection().getEnd(), getText().length());
                if (!NumberUtils.isNumber(newValue))
                    event.consume();
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
            return;
        }
        setNumber(NumberUtil.parse(input));
        selectAll();
    }
}