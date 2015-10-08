package org.menesty.ikea.ui.controls.form;

/**
 * Created by Menesty on
 * 6/21/14.
 * 0:04.
 */
public interface Field {
    boolean isValid();

    void reset();

    String getLabel();

    void setValid(boolean valid);
}
