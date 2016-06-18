package org.menesty.ikea.ui.table;

import org.menesty.ikea.ui.controls.form.NumberTextField;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 3/10/16.
 * 15:31.
 */
public class NumericEditableTableCell<S> extends AbstractEditableTableCell<S, Number, NumberTextField> {
  @Override
  protected void commitHelper(boolean losingFocus) {
    if (isValid(textField.getNumber())) {
      commitEdit(textField.getNumber());
    } else {
      cancelEdit();
    }
  }

  public boolean isValid(BigDecimal value) {
    return true;
  }

  @Override
  protected String getString() {
    return getItem() == null ? "" : getItem().toString();
  }

  @Override
  NumberTextField createTextField() {
    return new NumberTextField(null, true);
  }
}
