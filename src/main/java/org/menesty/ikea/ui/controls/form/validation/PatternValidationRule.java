package org.menesty.ikea.ui.controls.form.validation;

import java.util.regex.Pattern;

/**
 * Created by Menesty on
 * 11/2/15.
 * 06:38.
 */
public class PatternValidationRule implements ValidationRule<String> {
    private Pattern validationPattern;

    public PatternValidationRule(Pattern validationPattern) {
        this.validationPattern = validationPattern;
    }

    @Override
    public boolean validate(String value) {
        return value == null || validationPattern.matcher(value).find();
    }
}
