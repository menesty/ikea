package org.menesty.ikea.beans.value;

import javafx.beans.value.WritableNumberValue;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 3/10/16.
 * 16:18.
 */
public interface WritableBigDecimalValue extends WritableNumberValue {
  BigDecimal get();

  void set(BigDecimal var);

  void setValue(Number var);
}
