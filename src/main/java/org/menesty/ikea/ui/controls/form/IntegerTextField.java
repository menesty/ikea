package org.menesty.ikea.ui.controls.form;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang.StringUtils;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * User: Menesty
 * Date: 10/14/13
 * Time: 11:51 PM
 */
public class IntegerTextField extends TextField {

    private final NumberFormat nf;
    private SimpleIntegerProperty number = new SimpleIntegerProperty(0);


    public final Integer getNumber() {
        return number.get();
    }

    public final void setNumber(Integer value) {
        number.set(value);
    }

    public SimpleIntegerProperty numberProperty() {
        return number;
    }

    public IntegerTextField() {
        this(0);
    }


    public IntegerTextField(Integer value) {
        this(value, NumberFormat.getInstance());
        initHandlers();
    }

    public IntegerTextField(Integer value, NumberFormat nf) {
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
                if (!newValue)
                    parseAndFormatInput();

            }
        });
        numberProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                setText(newValue.toString());
            }
        });

        addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                String newValue = getText().substring(0, getSelection().getStart()) + event.getCharacter() + getText().substring(getSelection().getEnd(), getText().length());
                if (!StringUtils.isNumeric(newValue))
                    event.consume();
            }
        });

    }

    /**
     * Tries to parse the user input to a number according to the provided
     * NumberFormat
     */
    private void parseAndFormatInput() {
        try {
            String input = getText();
            if (input == null || input.length() == 0) {
                return;
            }
            Number parsedNumber = nf.parse(input);
            Integer newValue = parsedNumber.intValue();
            setNumber(newValue);
            selectAll();
        } catch (ParseException ex) {
            // If parsing fails keep old number
            setText(number.get() + "");
        }
    }
}