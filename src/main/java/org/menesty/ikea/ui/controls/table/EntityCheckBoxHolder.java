package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Created by Menesty on
 * 2/12/16.
 * 17:20.
 */
public class EntityCheckBoxHolder<T> {
  private BooleanProperty checked;

  private final T item;

  public EntityCheckBoxHolder(T item) {
    this(false, item);
  }

  public EntityCheckBoxHolder(boolean checked, T item) {
    this.item = item;
    this.checked = new SimpleBooleanProperty(checked);
  }

  public BooleanProperty checkedProperty() {
    return checked;
  }

  public boolean getChecked() {
    return checked.get();
  }

  public boolean isChecked() {
    return checked.get();
  }

  public T getItem() {
    return item;
  }

  public void setChecked(boolean checked) {
    this.checked.setValue(checked);
  }
}
