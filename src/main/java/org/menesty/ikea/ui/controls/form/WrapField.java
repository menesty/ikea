package org.menesty.ikea.ui.controls.form;

import javafx.scene.Node;
import javafx.scene.layout.VBox;


/**
 * Created by Menesty on
 * 10/1/15.
 * 21:40.
 */
public abstract class WrapField<T extends Node> extends VBox implements Field {
    private String label;
    protected T node;

    public WrapField(String label, T node) {
        getChildren().add(this.node = node);
        this.label = label;
    }

    @Override
    public void setValid(boolean valid) {
        node.getStyleClass().removeAll("validation-succeed", "validation-error");

        if (valid)
            node.getStyleClass().add("validation-succeed");
        else
            node.getStyleClass().add("validation-error");

    }

    @Override
    public String getLabel() {
        return label;
    }
}
