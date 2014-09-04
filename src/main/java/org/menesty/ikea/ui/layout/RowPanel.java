package org.menesty.ikea.ui.layout;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class RowPanel extends GridPane {

    private int currentRow = 0;
    private double labelWidth = 0;

    public RowPanel() {
        setPadding(new Insets(8));
        setHgap(5.0F);
        setVgap(5.0F);
        setPrefWidth(USE_PREF_SIZE);
    }

    public void setLabelWidth(double labelWidth) {
        this.labelWidth = labelWidth;
    }

    public void addRow(String labelText, Node field) {
        Label label = new Label(labelText);
        label.setId("proxy-dialog-label");

        if (labelWidth != 0)
            label.setPrefWidth(labelWidth);

        GridPane.setConstraints(label, 0, getCurrentRow());

        GridPane.setConstraints(field, 1, getCurrentRow(), 1, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
        getChildren().addAll(label, field);
        nextRow();
    }

    public void addRow(String labelText) {
        Label label = new Label(labelText);
        label.setId("proxy-dialog-label");
        GridPane.setConstraints(label, 0, getCurrentRow());
        getChildren().add(label);
        nextRow();
    }

    public void addRow(Node field) {
        addRow(field, 1);
    }

    public void addRow(Node field, int columnSpan) {
        GridPane.setConstraints(field, 0, getCurrentRow(), columnSpan, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
        getChildren().add(field);
        nextRow();
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public int nextRow() {
        return ++currentRow;
    }
}
