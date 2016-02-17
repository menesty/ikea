package org.menesty.ikea.ui.controls.table.component;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.menesty.ikea.ui.controls.table.EntityCheckBoxHolder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EntityCheckBoxTableColumn<S extends EntityCheckBoxHolder> extends TableColumn<S, Boolean> {
  List<CheckBox> list = new ArrayList<>();


  public EntityCheckBoxTableColumn() {
    CheckBox checked = new CheckBox();
    checked.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
      getTableView().getItems().forEach(entityCheckBoxHolder -> entityCheckBoxHolder.setChecked(newValue));
    });

    setMaxWidth(40);
    setGraphic(checked);
    setResizable(false);

    setCellFactory(p -> {
      CheckBoxTableCell<S, Boolean> checkBoxTableCell = new CheckBoxTableCell<>();
      checkBoxTableCell.setAlignment(Pos.CENTER);
      return checkBoxTableCell;
    });
  }
}
