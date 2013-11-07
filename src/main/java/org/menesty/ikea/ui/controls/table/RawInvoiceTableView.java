package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.util.NumberUtil;

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
            TableColumn<RawInvoiceProductItem, Number> column = new TableColumn<>();
            column.setMaxWidth(45);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RawInvoiceProductItem, Number>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<RawInvoiceProductItem, Number> item) {
                    return new SimpleIntegerProperty(item.getTableView().getItems().indexOf(item.getValue()) + 1);
                }
            });
            getColumns().add(column);
        }
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
            TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>();
            column.setText("Count");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RawInvoiceProductItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<RawInvoiceProductItem, String> item) {
                    return new SimpleStringProperty(NumberUtil.toString(item.getValue().getCount()));
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

        setRowFactory(new Callback<TableView<RawInvoiceProductItem>, TableRow<RawInvoiceProductItem>>() {
            @Override
            public TableRow<RawInvoiceProductItem> call(final TableView<RawInvoiceProductItem> rawInvoiceProductItemTableView) {
                final TableRow<RawInvoiceProductItem> row = new TableRow<>();
                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getClickCount() == 2)
                            onRowDoubleClick(row);
                    }
                });
                row.itemProperty().addListener(new ChangeListener<RawInvoiceProductItem>() {
                    @Override
                    public void changed(ObservableValue<? extends RawInvoiceProductItem> observableValue, RawInvoiceProductItem rawInvoiceProductItem, RawInvoiceProductItem newValue) {
                        if (newValue != null)
                            if (!newValue.getProductInfo().isVerified()) {
                                row.getStyleClass().add("productNotVerified");
                                return;
                            }
                        row.getStyleClass().remove("productNotVerified");
                    }
                });
                return row;
            }
        });
    }

    public void onRowDoubleClick(TableRow<RawInvoiceProductItem> row) {

    }
}
