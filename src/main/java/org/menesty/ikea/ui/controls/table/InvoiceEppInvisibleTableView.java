package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.ui.table.ProductBrowseColumn;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.NumberUtil;


/**
 * Created by Menesty on 12/22/13.
 */
public class InvoiceEppInvisibleTableView extends TableView<InvoiceItem> {
    public InvoiceEppInvisibleTableView() {
        {
            ProductBrowseColumn<InvoiceItem> column = new ProductBrowseColumn<>();
            column.setCellValueFactory(ColumnUtil.<InvoiceItem, String>column("originArtNumber"));
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Art # ");
            column.setMinWidth(105);
            column.setCellValueFactory(ColumnUtil.<InvoiceItem, String>column("artNumber"));
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("S Name");
            column.setMinWidth(170);
            column.setCellValueFactory(ColumnUtil.<InvoiceItem, String>column("shortName"));
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Count");
            column.setMaxWidth(50);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new SimpleStringProperty(NumberUtil.toString(item.getValue().getCount()));
                }
            });
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, Double> column = new TableColumn<>("Weight");
            column.setMaxWidth(55);
            column.setCellValueFactory(ColumnUtil.<InvoiceItem, Double>column("weight"));
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, Double> column = new TableColumn<>("Total");
            column.setMaxWidth(55);
            column.setCellValueFactory(ColumnUtil.<InvoiceItem, Double>column("priceWatTotal"));
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Size");
            column.setMinWidth(75);
            column.setCellValueFactory(ColumnUtil.<InvoiceItem, String>column("size"));
            getColumns().add(column);
        }

        setRowFactory(new Callback<TableView<InvoiceItem>, TableRow<InvoiceItem>>() {
            @Override
            public TableRow<InvoiceItem> call(final TableView<InvoiceItem> tableView) {
                final TableRow<InvoiceItem> row = new TableRow<>();

                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getClickCount() == 2)
                            onRowDoubleClick(row);
                    }
                });
                return row;
            }
        });
    }

    public void onRowDoubleClick(TableRow<InvoiceItem> row) {

    }
}