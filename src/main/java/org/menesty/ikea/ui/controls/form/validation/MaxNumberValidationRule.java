package org.menesty.ikea.ui.controls.form.validation;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 11/2/15.
 * 06:50.
 */
public class MaxNumberValidationRule implements ValidationRule<BigDecimal> {
    private BigDecimal maxValue;

    public MaxNumberValidationRule(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public boolean validate(BigDecimal value) {
        return value == null || maxValue.compareTo(value) >= 0;
    }
}
