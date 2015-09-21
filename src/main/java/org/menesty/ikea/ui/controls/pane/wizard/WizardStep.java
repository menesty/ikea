package org.menesty.ikea.ui.controls.pane.wizard;

/**
 * Created by Menesty on
 * 9/7/15.
 * 02:59.
 */
public interface WizardStep<T> {
    boolean isValid();

    boolean canSkip(T param);

    void collect(T param);

    void onActive(T param);
}

