package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.ui.controls.PathProperty;

public class OrderItemTableView extends TableView<OrderItem> {

    public OrderItemTableView() {
        {
            TableColumn<OrderItem, Number> column = new TableColumn<>();
            column.setMaxWidth(40);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, Number>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<OrderItem, Number> item) {
                    return new SimpleIntegerProperty(item.getTableView().getItems().indexOf(item.getValue()) + 1);
                }
            });
            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, String> column = new TableColumn<>("Art # ");
            column.setMinWidth(100);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderItem, String> item) {
                    return new PathProperty<>(item.getValue(), "artNumber");
                }
            });
            getColumns().add(column);
        }
        {
            TableColumn<OrderItem, String> column = new TableColumn<>("Name");
            column.setMinWidth(200);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderItem, String> item) {
                    return new PathProperty<>(item.getValue(), "name");
                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, String> column = new TableColumn<>("Count");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderItem, String> item) {
                    return new PathProperty<>(item.getValue(), "count");
                }
            });

            getColumns().add(column);
        }


        {
            TableColumn<OrderItem, Double> column = new TableColumn<>("Price");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<OrderItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "price");
                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, Double> column = new TableColumn<>("T Price");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<OrderItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "total");
                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, OrderItem.Type> column = new TableColumn<>("Type");
            column.setMinWidth(100);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, OrderItem.Type>, ObservableValue<OrderItem.Type>>() {
                @Override
                public ObservableValue<OrderItem.Type> call(TableColumn.CellDataFeatures<OrderItem, OrderItem.Type> item) {
                    return new PathProperty<>(item.getValue(), "type");
                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, ProductInfo.Group> column = new TableColumn<>("Group");
            column.setMinWidth(100);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, ProductInfo.Group>, ObservableValue<ProductInfo.Group>>() {
                @Override
                public ObservableValue<ProductInfo.Group> call(TableColumn.CellDataFeatures<OrderItem, ProductInfo.Group> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.group");
                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, ProductInfo.Group> column = new TableColumn<>("P Price");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, ProductInfo.Group>, ObservableValue<ProductInfo.Group>>() {
                @Override
                public ObservableValue<ProductInfo.Group> call(TableColumn.CellDataFeatures<OrderItem, ProductInfo.Group> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.price");
                }
            });

            getColumns().add(column);
        }


        setRowFactory(new Callback<TableView<OrderItem>, TableRow<OrderItem>>() {
            @Override
            public TableRow<OrderItem> call(final TableView<OrderItem> tableView) {
                final TableRow<OrderItem> row = new TableRow<>();

                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getClickCount() == 2) {
                            onRowDoubleClick(row);
                        }
                    }
                });
                row.itemProperty().addListener(new ChangeListener<OrderItem>() {
                    @Override
                    public void changed(ObservableValue<? extends OrderItem> observableValue, OrderItem oldValue, OrderItem newValue) {
                        row.getStyleClass().remove("productNotVerified");
                        if (newValue != null && OrderItem.Type.Na != newValue.getType() && !newValue.getProductInfo().isVerified())
                            row.getStyleClass().add("productNotVerified");

                    }
                });

                return row;
            }
        });
    }

    public void onRowDoubleClick(TableRow<OrderItem> row) {

    }
}
