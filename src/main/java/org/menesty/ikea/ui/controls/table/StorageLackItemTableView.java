package org.menesty.ikea.ui.controls.table;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
import org.menesty.ikea.domain.StorageLack;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.util.NumberUtil;

public class StorageLackItemTableView extends TableView<StorageLack> {

    public StorageLackItemTableView() {
        {
            TableColumn<StorageLack, Number> column = new TableColumn<>();
            column.setMaxWidth(45);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageLack, Number>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<StorageLack, Number> item) {
                    return new SimpleIntegerProperty(item.getTableView().getItems().indexOf(item.getValue()) + 1);
                }
            });
            getColumns().add(column);
        }

        {
            TableColumn<StorageLack, String> column = new TableColumn<>("Art # ");
            column.setMinWidth(100);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageLack, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<StorageLack, String> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.artNumber");
                }
            });
            getColumns().add(column);
        }

        {
            TableColumn<StorageLack, String> column = new TableColumn<>("Name");
            column.setMinWidth(200);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageLack, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<StorageLack, String> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.name");
                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<StorageLack, String> column = new TableColumn<>("Count");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageLack, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<StorageLack, String> item) {
                    return new SimpleStringProperty(NumberUtil.toString(item.getValue().getCount()));
                }
            });

            getColumns().add(column);
        }
        setRowFactory(new Callback<TableView<StorageLack>, TableRow<StorageLack>>() {
            @Override
            public TableRow<StorageLack> call(TableView<StorageLack> storageLackTableView) {
                final TableRow<StorageLack> row = new TableRow<>();
                row.itemProperty().addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        row.getStyleClass().remove("productError");
                        row.setTooltip(null);

                        if(row.getItem() != null && !row.getItem().isExist()){
                            row.getStyleClass().add("productError");
                            row.setTooltip(new Tooltip("This product not exist in order"));
                        }
                    }
                });
                return row;
            }
        });
    }
}
