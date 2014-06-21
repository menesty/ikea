package org.menesty.ikea.ui.controls.form;

import javafx.scene.Node;
import org.menesty.ikea.ui.layout.RowPanel;

import java.util.ArrayList;
import java.util.List;

public class FormPane extends RowPanel {
    private List<Field> fields = new ArrayList<>();

    public <T extends Node & Field> void add(T field, int span) {
        fields.add(field);
        addRow(field, span);
    }

    public <T extends Node & Field> void add(T field) {
        fields.add(field);
        addRow(field.getLabel(), field);
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
}
