package org.menesty.ikea.ui.controls.form;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang.math.NumberUtils;
import org.menesty.ikea.ui.controls.form.validation.MaxNumberValidationRule;
import org.menesty.ikea.ui.controls.form.validation.MinNumberValidationRule;
import org.menesty.ikea.ui.controls.form.validation.ValidationRule;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Menesty
 * Date: 10/14/13
 * Time: 11:51 PM
 */
public class NumberTextField extends TextField {
  private final static List<KeyCode> numberList = new ArrayList<>();

  static {
    numberList.add(KeyCode.DIGIT0);
    numberList.add(KeyCode.DIGIT1);
    numberList.add(KeyCode.DIGIT2);
    numberList.add(KeyCode.DIGIT3);
    numberList.add(KeyCode.DIGIT4);
    numberList.add(KeyCode.DIGIT5);
    numberList.add(KeyCode.DIGIT6);
    numberList.add(KeyCode.DIGIT7);
    numberList.add(KeyCode.DIGIT8);
    numberList.add(KeyCode.DIGIT9);
  }

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

  public void setMaxValue(BigDecimal maxValue) {
    ValidationRule rule = findValidationRule(MaxNumberValidationRule.class);

    if (maxValue == null) {
      validationRules.remove(rule);
    } else if (rule == null) {
      validationRules.add(new MaxNumberValidationRule(maxValue));
    }
  }

  public void setMinValue(BigDecimal minValue) {
    ValidationRule rule = findValidationRule(MinNumberValidationRule.class);

    if (minValue == null) {
      validationRules.remove(rule);
    } else if (rule == null) {
      validationRules.add(new MinNumberValidationRule(minValue));
    }
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

  @Override
  public Object getValue() {
    return getNumber();
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
      textField.setText((allowDouble ? newValue.doubleValue() + "" : newValue.intValue() + ""));
    else
      textField.setText("0");
  }

  private void initHandlers() {
    // try to parse when focus is lost or RETURN is hit
    textField.setOnAction(arg0 -> parseAndFormatInput(false));

    textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue)
        parseAndFormatInput(true);
    });

    textField.textProperty().addListener((observable, oldValue1, newValue1) -> {
      parseAndFormatInput(false);
    });

    // Set text in field if BigDecimal property is changed from outside.
    numberProperty().addListener((obserable, oldValue, newValue) -> {
      updateView();
    });

    textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
      if (!allowDouble && KeyCode.PERIOD == event.getCode()) {
        event.consume();
        return;
      }
      String newValue = "";

      if (getText() != null) {
        newValue = textField.getText().substring(0, textField.getSelection().getStart()) + event.getCharacter() +
            textField.getText().substring(textField.getSelection().getEnd(), textField.getText().length());
      } else {
        newValue = event.getCharacter();
      }

      if (NumberUtils.isNumber(newValue) || event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE ||
          event.getCode() == KeyCode.TAB) {

        return;
      }

      event.consume();
    });

    textField.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.DIGIT0)
        parseAndFormatInput(false);
    });

  }

  /**
   * Tries to parse the user input to a number according to the provided
   * NumberFormat
   */
  private void parseAndFormatInput(boolean selectAll) {
    try {
      String input = getText();

      if (input == null || input.length() == 0) {
        setNumber(null);
        return;
      }

      int caretPosition = textField.getCaretPosition();

      if (caretPosition > input.length()) {
        caretPosition = input.length();
      }

      Number parsedNumber = nf.parse(input);
      BigDecimal newValue = new BigDecimal(allowDouble ? parsedNumber.doubleValue() + "" : parsedNumber.intValue() + "");
      setNumber(newValue);

      if (selectAll) {
        textField.selectAll();
      } else {
        textField.positionCaret(caretPosition);
      }
    } catch (ParseException ex) {
      // If parsing fails keep old number
      textField.setText((allowDouble ? number.get().doubleValue() : number.get().intValue()) + "");
    }
  }

}