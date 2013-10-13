package org.menesty.ikea.ui.controls.table;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.ui.controls.PathProperty;

/**
 * User: Menesty
 * Date: 10/13/13
 * Time: 6:01 PM
 */
public class RawInvoiceTableView extends TableView<RawInvoiceProductItem> {

    public RawInvoiceTableView() {
        super();
        initColumns();
    }

    private void initColumns() {
        {
            TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>();
            column.setText("Art # ");
            column.setMinWidth(100);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RawInvoiceProductItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<RawInvoiceProductItem, String> item) {
                    return new PathProperty<>(item.getValue(), "artNumber");
                }
            });
            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>();
            column.setText("Name");
            column.setMinWidth(200);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RawInvoiceProductItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<RawInvoiceProductItem, String> item) {
                    return new PathProperty<>(item.getValue(), "name");
                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceProductItem, Integer> column = new TableColumn<>();
            column.setText("Count");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RawInvoiceProductItem, Integer>, ObservableValue<Integer>>() {
                @Override
                public ObservableValue<Integer> call(TableColumn.CellDataFeatures<RawInvoiceProductItem, Integer> item) {
                    return new PathProperty<>(item.getValue(), "count");
                }
            });

            getColumns().add(column);
        }


        {
            TableColumn<RawInvoiceProductItem, Double> column = new TableColumn<>();
            column.setText("Price");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RawInvoiceProductItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<RawInvoiceProductItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "price");
                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>();
            column.setText("Wat");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RawInvoiceProductItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<RawInvoiceProductItem, String> item) {
                    return new PathProperty<>(item.getValue(), "wat");
                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceProductItem, Double> column = new TableColumn<>();
            column.setText("T Price");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RawInvoiceProductItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<RawInvoiceProductItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "total");
                }
            });

            getColumns().add(column);
        }
    }
}
