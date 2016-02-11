package org.menesty.ikea.ui.controls.form.validation;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 11/2/15.
 * 06:50.
 */
public class MinNumberValidationRule implements ValidationRule<BigDecimal> {
    private BigDecimal minValue;

    public MinNumberValidationRule(BigDecimal minValue) {
        this.minValue = minValue;
    }

    @Override
    public boolean validate(BigDecimal value) {
        return value != null && minValue.compareTo(value) <= 0;
    }
}
