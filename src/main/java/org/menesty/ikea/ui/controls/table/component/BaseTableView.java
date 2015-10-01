package org.menesty.ikea.ui.controls.table.component;

import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

public class BaseTableView<Entity> extends TableView<Entity> {
    public interface RowDoubleClickListener<Entity> {
        void onRowDoubleClick(TableRow<Entity> row);
    }

    public interface RowRenderListener<Entity> {
        void onRowRender(TableRow<Entity> row, Entity newValue);
    }

    private List<TableRow<Entity>> rows = new ArrayList<>();

    private RowDoubleClickListener<Entity> rowDoubleClickListener;
    private RowRenderListener<Entity> rowRenderListener;

    public BaseTableView() {
        setRowFactory(entityTableView -> {
            final TableRow<Entity> row = new TableRow<>();
            rows.add(row);

            row.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2) {
                    onRowDoubleClick(row);
                }
            });

            row.itemProperty().addListener((observableValue, oldValue, newValue) -> {
                onRowRender(row, newValue);
            });

            return row;
        });
    }

    protected void onRowRender(TableRow<Entity> row, Entity newValue) {
        if (rowRenderListener != null) {
            rowRenderListener.onRowRender(row, newValue);
        }
    }

    protected void onRowDoubleClick(TableRow<Entity> row) {
        if (rowDoubleClickListener != null) {
            rowDoubleClickListener.onRowDoubleClick(row);
        }
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

    public void setRowDoubleClickListener(RowDoubleClickListener<Entity> rowDoubleClickListener) {
        this.rowDoubleClickListener = rowDoubleClickListener;
    }

    public void setRowRenderListener(RowRenderListener<Entity> rowRenderListener) {
        this.rowRenderListener = rowRenderListener;
    }
}

