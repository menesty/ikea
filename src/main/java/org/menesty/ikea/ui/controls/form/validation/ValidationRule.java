package org.menesty.ikea.ui.controls.form.validation;

/**
 * Created by Menesty on
 * 11/2/15.
 * 06:16.
 */
public interface ValidationRule<T> {
    boolean validate(T value);
}
