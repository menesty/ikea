package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;
import org.menesty.ikea.util.NumberUtil;


/**
 * Created by Menesty on 12/22/13.
 */
public class InvoiceEppInvisibleTableView extends TableView<InvoiceItem> {
    public InvoiceEppInvisibleTableView() {
        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>();
            column.setMaxWidth(25);
            column.setCellFactory(new Callback<TableColumn<InvoiceItem, String>, TableCell<InvoiceItem, String>>() {
                @Override
                public TableCell<InvoiceItem, String> call(TableColumn<InvoiceItem, String> invoiceItemNumberTableColumn) {
                    TableCell<InvoiceItem, String> tableCell = new TableCell<InvoiceItem, String>() {
                        private ImageView imageView;
                        private HBox content;
                        @Override
                        protected void updateItem(final String artNumber, boolean empty) {
                            super.updateItem(artNumber, empty);
                            if (empty) {
                                setGraphic(null);
                                setText(null);
                            } else {
                                if (imageView == null) {
                                    imageView = ImageFactory.createWeb16Icon();
                                    content = new HBox();
                                    content.getChildren().add(imageView);

                                }

                                imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                    @Override
                                    public void handle(MouseEvent mouseEvent) {
                                        ProductDialog.browse(artNumber);
                                    }
                                });
                                setGraphic(content);
                            }
                        }

                    };
                    tableCell.setAlignment(Pos.CENTER);
                    return tableCell;
                }
            });
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new PathProperty<>(item.getValue(), "originArtNumber");
                }
            });
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Art # ");
            column.setMinWidth(105);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new PathProperty<>(item.getValue(), "artNumber");
                }
            });
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("S Name");
            column.setMinWidth(170);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new PathProperty<>(item.getValue(), "shortName");
                }
            });
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
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<InvoiceItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "weight");
                }
            });
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, Double> column = new TableColumn<>("Total");
            column.setMaxWidth(55);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<InvoiceItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "priceWatTotal");
                }
            });
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Size");
            column.setMinWidth(75);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new PathProperty<>(item.getValue(), "size");
                }
            });
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