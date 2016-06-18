package org.menesty.ikea.ui.controls.form;

import javafx.scene.control.Label;

/**
 * Created by Menesty on
 * 11/2/15.
 * 08:52.
 */
public class LabelField extends Label implements Field {
    private String label;

    public LabelField(String label) {
        this.label = label;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void reset() {
        setText(null);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setValid(boolean valid) {
    }
}
