package org.menesty.ikea.ui.layout;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RowPanel extends GridPane {
    private int currentRow = 0;
    private double labelWidth = 0;
    private List<Row> rows = new ArrayList<>();

    public RowPanel() {
        setPadding(new Insets(8));
        setHgap(5.0F);
        setVgap(5.0F);
        setPrefWidth(USE_PREF_SIZE);
        //setGridLinesVisible(true);
    }

    public void setLabelWidth(double labelWidth) {
        this.labelWidth = labelWidth;
    }

    public void addRow(String labelText, Node field) {
        addRow(labelText, field, getCurrentRow());
        nextRow();
    }

    private void addRow(String labelText, Node field, int row) {
        Label label = new Label(labelText);
        label.setId("proxy-dialog-label");

        if (labelWidth != 0)
            label.setPrefWidth(labelWidth);

        GridPane.setConstraints(label, 0, row, 1, 1, HPos.LEFT, VPos.CENTER);

        GridPane.setConstraints(field, 1, row, 1, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
        getChildren().addAll(label, field);
        rows.add(new Row(row, label, field));
    }

    public void addRow(String labelText) {
        Label label = new Label(labelText);
        label.setId("proxy-dialog-label");
        GridPane.setConstraints(label, 0, getCurrentRow());
        getChildren().add(label);

        rows.add(new Row(getCurrentRow(), label));
        nextRow();
    }

    public void addRow(Node field) {
        addRow(field, 1);
    }

    public void addRow(Node field, int columnSpan) {
        GridPane.setConstraints(field, 0, getCurrentRow(), columnSpan, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
        getChildren().add(field);
        rows.add(new Row(getCurrentRow(), field));
        nextRow();
    }

    public void setVisibleRow(int index, boolean visible) {
        Row currentRow = rows.stream().filter(row -> row.getIndex() == index).findFirst().get();

        if (currentRow.isVisible() != visible) {
            currentRow.setVisible(visible);

            if (visible) {
                getChildren().addAll(currentRow.getElements());
            } else {
                getChildren().removeAll(currentRow.getElements());
            }
        }

    }

    public int getCurrentRow() {
        return currentRow;
    }

    public int nextRow() {
        return ++currentRow;
    }

    class Row {
        private final int index;
        private boolean visible = true;
        private final List<Node> elements;

        public Row(int index, Node... nodes) {
            this.index = index;
            this.elements = Arrays.asList(nodes);
        }

        public int getIndex() {
            return index;
        }

        public List<Node> getElements() {
            return elements;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }
}
