package org.menesty.ikea.ui.controls.form.validation;

/**
 * Created by Menesty on
 * 7/14/16.
 * 16:19.
 */
public class MaxStringLength implements ValidationRule<String> {
  private final int maxLength;

  public MaxStringLength(int maxLength) {
    this.maxLength = maxLength;
  }

  @Override
  public boolean validate(String value) {
    return value == null || value.length() <= maxLength;
  }
}
