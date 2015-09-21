package org.menesty.ikea.ui.controls.form;

import javafx.scene.Node;
import org.menesty.ikea.ui.layout.RowPanel;

import java.util.ArrayList;
import java.util.List;

public class FormPane extends RowPanel {
    private List<Field> fields = new ArrayList<>();

    private boolean showLabels = true;

    public <T extends Node & Field> void add(T field, int span) {
        fields.add(field);
        addRow(field, span);
    }

    public <T extends Node & Field> void add(T field) {
        fields.add(field);

        if (showLabels)
            addRow(field.getLabel(), field);
        else
            addRow(field);
    }

    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
    }

    public boolean isValid() {
        boolean result = true;

        for (Field field : fields)
            if (!field.isValid())
                result = false;

        return result;
    }

    public void reset() {
        for (Field field : fields)
            field.reset();
    }

    public <T extends Node & Field> void setVisible(T field, boolean visible) {
        int index = fields.indexOf(field);

        if (index != -1) {
            setVisibleRow(index, visible);
        }
    }
}
