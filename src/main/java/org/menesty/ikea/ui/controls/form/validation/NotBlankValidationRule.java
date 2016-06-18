package org.menesty.ikea.ui.controls.form.validation;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Menesty on
 * 11/2/15.
 * 06:19.
 */
public class NotBlankValidationRule implements ValidationRule<Object> {
  @Override
  public boolean validate(Object value) {
    return value != null && StringUtils.isNotBlank(value.toString());
  }
}
