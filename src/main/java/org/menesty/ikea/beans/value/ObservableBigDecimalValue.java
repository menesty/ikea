package org.menesty.ikea.beans.value;

import javafx.beans.value.ObservableNumberValue;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 3/10/16.
 * 15:50.
 */
public interface ObservableBigDecimalValue extends ObservableNumberValue {
  BigDecimal get();
}
