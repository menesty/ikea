package org.menesty.ikea.ui.controls.form;

import org.menesty.ikea.ui.layout.RowPanel;

import java.util.ArrayList;
import java.util.List;

public class FormPane extends RowPanel {
    private List<TextField> fields = new ArrayList<>();

    public void add(TextField field) {
        fields.add(field);
        addRow(field.getLabel(), field);
    }

    public boolean isValid() {
        boolean result = true;

        for (TextField field : fields)
            if (!field.isValid())
                result = false;

        return result;
    }

    public void reset() {
        for (TextField field : fields)
            field.reset();
    }
}
