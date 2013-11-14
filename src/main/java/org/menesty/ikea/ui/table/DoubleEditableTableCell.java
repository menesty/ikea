package org.menesty.ikea.ui.table;

import org.menesty.ikea.ui.controls.form.DoubleTextField;
import org.menesty.ikea.util.NumberUtil;

/**
 * User: Menesty
 * Date: 11/13/13
 * Time: 7:41 PM
 */
public class DoubleEditableTableCell<S extends Object> extends AbstractEditableTableCell<S, Double, DoubleTextField> {
    @Override
    protected void commitHelper(boolean losingFocus) {
        if (textField == null) {
            return;
        }
        commitEdit(textField.getNumber());

    }

    @Override
    public void startEdit() {
        super.startEdit();
        textField.setNumber(getItem());
    }

    @Override
    protected String getString() {
        return getItem() == null ? "" : NumberUtil.toString(getItem());
    }

    @Override
    DoubleTextField createTextField() {
        return new DoubleTextField();
    }
}
