package org.menesty.ikea.ui.controls.table.component;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseTableView<Entity> extends TableView<Entity> {
    private List<TableRow<Entity>> rows = new ArrayList<>();

    public BaseTableView() {
        setRowFactory(entityTableView -> {
            final TableRow<Entity> row = new TableRow<>();
            rows.add(row);

            row.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2)
                    onRowDoubleClick(row);
            });

            row.itemProperty().addListener((observableValue, oldValue, newValue) -> {
                onRowRender(row, newValue);
            });

            return row;
        });
    }

    protected void onRowRender(TableRow<Entity> row, Entity newValue) {

    }

    protected void onRowDoubleClick(TableRow<Entity> row) {

    }

    public void updateRows() {
        for (TableRow<Entity> row : rows)
            row.setItem(null);
    }

    public void update(Entity entity) {
        for (TableRow<Entity> row : rows)
            if (entity.equals(row.getItem())) {
                row.setItem(null);
                return;
            }
    }

}
