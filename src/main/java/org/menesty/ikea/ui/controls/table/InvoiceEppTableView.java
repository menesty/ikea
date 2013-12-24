package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.table.DoubleEditableTableCell;
import org.menesty.ikea.util.NumberUtil;

/**
 * Created by Menesty on 12/22/13.
 */
public abstract class InvoiceEppTableView extends TableView<InvoiceItem> {

    public InvoiceEppTableView() {
        {
            TableColumn<InvoiceItem, Number> column = new TableColumn<>();
            column.setMaxWidth(35);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, Number>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<InvoiceItem, Number> item) {
                    return new SimpleIntegerProperty(item.getTableView().getItems().indexOf(item.getValue()) + 1);
                }
            });
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Art # ");
            column.setMinWidth(110);
            column.setCellFactory(TextFieldTableCell.<InvoiceItem>forTableColumn());
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new PathProperty<>(item.getValue(), "artNumber");
                }
            });
            column.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<InvoiceItem, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<InvoiceItem, String> t) {
                    InvoiceItem item = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    if (!item.getArtNumber().equals(t.getNewValue())) {
                        item.setArtNumber(t.getNewValue());
                        onEdit(item);
                    }

                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("S Name");
            column.setMinWidth(170);
            column.setCellFactory(TextFieldTableCell.<InvoiceItem>forTableColumn());
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new PathProperty<>(item.getValue(), "shortName");
                }
            });
            column.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<InvoiceItem, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<InvoiceItem, String> t) {
                    InvoiceItem item = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    if (!item.getShortName().equals(t.getNewValue())) {
                        item.setShortName(t.getNewValue());
                        onEdit(item);
                    }

                }
            });

            getColumns().add(column);
        }

        Callback<TableColumn<InvoiceItem, Double>, TableCell<InvoiceItem, Double>> doubleFactory = new Callback<TableColumn<InvoiceItem, Double>, TableCell<InvoiceItem, Double>>() {
            @Override
            public TableCell<InvoiceItem, Double> call(TableColumn p) {
                return new DoubleEditableTableCell<>();
            }
        };

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Count");
            column.setMaxWidth(50);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new SimpleStringProperty(NumberUtil.toString(item.getValue().getCount()));
                }
            });
            //column.setCellFactory(doubleFactory);
            getColumns().add(column);
        }


        {
            TableColumn<InvoiceItem, Double> column = new TableColumn<>("Price");
            column.setMaxWidth(70);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<InvoiceItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "priceWat");
                }
            });
            column.setCellFactory(doubleFactory);
            column.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<InvoiceItem, Double>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<InvoiceItem, Double> t) {
                    InvoiceItem item = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    if (item.getPriceWat() != t.getNewValue()) {
                        item.setPrice(t.getNewValue());
                        onEdit(item);
                    }

                }
            });
            getColumns().add(column);
        }

        setEditable(true);
    }

    public abstract void onEdit(InvoiceItem item);
}
