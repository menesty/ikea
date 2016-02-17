package org.menesty.ikea.ui.controls.table.component;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CheckBoxTableColumn<S> extends TableColumn<S, Boolean> {
    List<CheckBox> list = new ArrayList<>();


    public CheckBoxTableColumn() {
        CheckBox checked = new CheckBox();
        checked.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            for (int i = 0; i < list.size(); i++) {
                CheckBox item = list.get(i);
                //item.setItem(newValue);
                item.selectedProperty().setValue(newValue);
            }
        });

        final Field checkBoxFiled = getField();

        setMaxWidth(40);
        setGraphic(checked);
        setResizable(false);

        setCellFactory(p -> {
            CheckBoxTableCell<S, Boolean> checkBoxTableCell = new CheckBoxTableCell<>();
            checkBoxTableCell.setAlignment(Pos.CENTER);
            list.add(getCheckBox(checkBoxFiled, checkBoxTableCell));
            return checkBoxTableCell;
        });
    }

    private Field getField() {
        try {
            return CheckBoxTableCell.class.getDeclaredField("checkBox");
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private CheckBox getCheckBox(Field checkBoxFiled, CheckBoxTableCell<S, Boolean> cell) {
        checkBoxFiled.setAccessible(true);
        try {
            return (CheckBox) checkBoxFiled.get(cell);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
