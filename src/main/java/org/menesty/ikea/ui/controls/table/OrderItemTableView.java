package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.menesty.ikea.domain.OrderItem;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.NumberUtil;

public class OrderItemTableView extends TableView<OrderItem> {

    public OrderItemTableView() {
        {
            TableColumn<OrderItem, Number> column = new TableColumn<>();
            column.setMaxWidth(40);
            column.setCellValueFactory(ColumnUtil.<OrderItem>indexColumn());
            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, String> column = new TableColumn<>("Art # ");
            column.setMinWidth(85);
            column.setCellValueFactory(ColumnUtil.<OrderItem, String>column("artNumber"));
            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, String> column = new TableColumn<>("Name");
            column.setPrefWidth(300);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderItem, String> item) {
                    String field = "name";

                    if (item.getValue().getProductInfo() != null && item.getValue().getProductInfo().getShortName() != null)
                        field = "productInfo.shortName";

                    return new PathProperty<>(item.getValue(), field);
                }
            });

            getColumns().add(column);
        }
        {
            TableColumn<OrderItem, String> column = new TableColumn<>("Ua Name");
            column.setPrefWidth(200);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderItem, String> item) {
                    if (item.getValue().getProductInfo() != null && item.getValue().getProductInfo().getUaName() != null)
                        return new PathProperty<>(item.getValue(), "productInfo.uaName");
                    return new SimpleStringProperty("");
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
                    return new SimpleStringProperty(NumberUtil.toString(item.getValue().getCount()));
                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, Double> column = new TableColumn<>("XLS Price");
            column.setMinWidth(60);
            column.setCellValueFactory(ColumnUtil.<OrderItem, Double>column("price"));

            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, Double> column = new TableColumn<>("T Price");
            column.setMinWidth(60);
            column.setCellValueFactory(ColumnUtil.<OrderItem, Double>column("total"));

            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, OrderItem.Type> column = new TableColumn<>("Type");
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<OrderItem, OrderItem.Type>column("type"));

            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, ProductInfo.Group> column = new TableColumn<>("Group");
            column.setMinWidth(80);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, ProductInfo.Group>, ObservableValue<ProductInfo.Group>>() {
                @Override
                public ObservableValue<ProductInfo.Group> call(TableColumn.CellDataFeatures<OrderItem, ProductInfo.Group> item) {
                    if (item.getValue().getProductInfo() != null)
                        return new PathProperty<>(item.getValue(), "productInfo.group");

                    return new SimpleObjectProperty<>(null);
                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<OrderItem, Double> column = new TableColumn<>("Ikea Price");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<OrderItem, Double> item) {
                    if (item.getValue().getProductInfo() != null)
                        return new PathProperty<>(item.getValue(), "productInfo.price");

                    return new SimpleObjectProperty<>(null);
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
                        if (mouseEvent.getClickCount() == 2)
                            onRowDoubleClick(row);

                    }
                });
                row.itemProperty().addListener(new ChangeListener<OrderItem>() {
                    @Override
                    public void changed(ObservableValue<? extends OrderItem> observableValue, OrderItem oldValue, OrderItem newValue) {
                        row.getStyleClass().removeAll("productNotVerified", "productError");
                        row.setContextMenu(null);

                        if (newValue != null) {
                            if (OrderItem.Type.Na != newValue.getType() && !row.getItem().isInvalidFetch()
                                    && !newValue.getProductInfo().isVerified())
                                row.getStyleClass().add("productNotVerified");

                            else if (OrderItem.Type.Na != newValue.getType() && row.getItem().isInvalidFetch()) {
                                row.getStyleClass().add("productError");

                                ContextMenu contextMenu = new ContextMenu();
                                {
                                    MenuItem menuItem = new MenuItem("Fetch", ImageFactory.createFetch16Icon());
                                    menuItem.setOnAction(new EventHandler<ActionEvent>() {
                                        @Override
                                        public void handle(ActionEvent actionEvent) {
                                            onFetchAction(row);
                                        }
                                    });
                                    contextMenu.getItems().add(menuItem);
                                }

                                Menu statusMenu = new Menu("Status");
                                {
                                    MenuItem menuItem = new MenuItem("NA");
                                    menuItem.setOnAction(new EventHandler<ActionEvent>() {
                                        @Override
                                        public void handle(ActionEvent actionEvent) {
                                            row.getItem().setType(OrderItem.Type.Na);
                                            row.setItem(null);
                                        }
                                    });
                                    statusMenu.getItems().add(menuItem);
                                }
                                contextMenu.getItems().add(statusMenu);

                                {
                                    MenuItem menuItem = new MenuItem("Browse", ImageFactory.createWeb16Icon());
                                    menuItem.setOnAction(new EventHandler<ActionEvent>() {
                                        @Override
                                        public void handle(ActionEvent actionEvent) {
                                            ProductDialog.browse(row.getItem().getArtNumber());
                                        }
                                    });
                                    contextMenu.getItems().add(menuItem);
                                }

                                row.setContextMenu(contextMenu);
                            }

                        }
                    }
                });

                return row;
            }
        });
    }

    public void onFetchAction(TableRow<OrderItem> row) {

    }

    public void onRowDoubleClick(TableRow<OrderItem> row) {

    }
}
