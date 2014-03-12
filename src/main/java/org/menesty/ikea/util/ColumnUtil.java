package org.menesty.ikea.util;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.menesty.ikea.ui.controls.PathProperty;

public class ColumnUtil {

    public static <Entity, Value> Callback<TableColumn.CellDataFeatures<Entity, Value>, ObservableValue<Value>> column(final String propertyName) {
        return new Callback<TableColumn.CellDataFeatures<Entity, Value>, ObservableValue<Value>>() {

            @Override
            public ObservableValue<Value> call(TableColumn.CellDataFeatures<Entity, Value> item) {
                return new PathProperty<>(item.getValue(), propertyName);
            }
        };
    }

    public static <Entity> Callback<TableColumn.CellDataFeatures<Entity, Number>, ObservableValue<Number>> indexColumn() {
        return new Callback<TableColumn.CellDataFeatures<Entity, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<Entity, Number> item) {
                return new SimpleIntegerProperty(item.getTableView().getItems().indexOf(item.getValue()) + 1);
            }
        };
    }
}
