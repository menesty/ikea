package org.menesty.ikea.beans.property;

import com.sun.javafx.binding.ExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;

/**
 * Created by Menesty on
 * 3/10/16.
 * 16:09.
 */
public abstract class ReadOnlyBigDecimalPropertyBase  extends ReadOnlyBigDecimalProperty {

  ExpressionHelper<Number> helper;

  @Override
  public void addListener(InvalidationListener listener) {
    helper = ExpressionHelper.addListener(helper, this, listener);
  }

  @Override
  public void removeListener(InvalidationListener listener) {
    helper = ExpressionHelper.removeListener(helper, listener);
  }

  @Override
  public void addListener(ChangeListener<? super Number> listener) {
    helper = ExpressionHelper.addListener(helper, this, listener);
  }

  @Override
  public void removeListener(ChangeListener<? super Number> listener) {
    helper = ExpressionHelper.removeListener(helper, listener);
  }

  /**
   * Sends notifications to all attached
   * {@link javafx.beans.InvalidationListener InvalidationListeners} and
   * {@link javafx.beans.value.ChangeListener ChangeListeners}.
   *
   * This method needs to be called, if the value of this property changes.
   */
  protected void fireValueChangedEvent() {
    ExpressionHelper.fireValueChangedEvent(helper);
  }

}
