package org.menesty.ikea.ui.pages;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.Order;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.InvoicePdfService;
import org.menesty.ikea.service.OrderService;
import org.menesty.ikea.ui.controls.PathProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Menesty
 * Date: 10/11/13
 * Time: 9:51 PM
 */
public class OrderViewPage extends BasePage {
    TableView<OrderItem> tableView;
    OrderService orderService;
    private Region maskRegion;
    private ProgressIndicator progressIndicator;
    private InvoicePdfService invoicePdfService;

    private Order currentOrder;
    private TableView<RawInvoiceProductItem> rawInvoiceItemTableView;
    private TableView<InvoicePdfTableItem> invoicePfdTableView;

    public OrderViewPage() {
        super("Order");
        orderService = new OrderService();
        invoicePdfService = new InvoicePdfService();
    }

    @Override
    public Node createView() {
        maskRegion = new Region();
        maskRegion.setVisible(false);
        maskRegion.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4)");
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(150, 150);
        progressIndicator.setVisible(false);
        Pane pane = createRoot();

        StackPane stack = new StackPane();
        stack.getChildren().addAll(createInvoiceView(), maskRegion, progressIndicator);
        pane.getChildren().add(stack);
        return pane;
    }

    private Tab createOrderItemTab() {
        tableView = new TableView<>();
        {
            TableColumn<OrderItem, Number> column = new TableColumn<>();
            column.setText("");
            column.setMinWidth(30);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, Number>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<OrderItem, Number> item) {
                    return new SimpleIntegerProperty(item.getTableView().getItems().indexOf(item.getValue()) + 1);
                }
            });
            tableView.getColumns().add(column);
        }

        {
            TableColumn<OrderItem, String> column = new TableColumn<>();
            column.setText("Art # ");
            column.setMinWidth(100);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderItem, String> item) {
                    return new PathProperty<>(item.getValue(), "artNumber");
                }
            });
            tableView.getColumns().add(column);
        }
        {
            TableColumn<OrderItem, String> column = new TableColumn<>();
            column.setText("Name");
            column.setMinWidth(200);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderItem, String> item) {
                    return new PathProperty<>(item.getValue(), "name");
                }
            });

            tableView.getColumns().add(column);
        }

        {
            TableColumn<OrderItem, String> column = new TableColumn<>();
            column.setText("Count");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderItem, String> item) {
                    return new PathProperty<>(item.getValue(), "count");
                }
            });

            tableView.getColumns().add(column);
        }


        {
            TableColumn<OrderItem, Double> column = new TableColumn<>();
            column.setText("Price");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<OrderItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "price");
                }
            });

            tableView.getColumns().add(column);
        }

        {
            TableColumn<OrderItem, Double> column = new TableColumn<>();
            column.setText("T Price");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<OrderItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "total");
                }
            });

            tableView.getColumns().add(column);
        }

        {
            TableColumn<OrderItem, OrderItem.Type> column = new TableColumn<>();
            column.setText("Type");
            column.setMinWidth(100);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, OrderItem.Type>, ObservableValue<OrderItem.Type>>() {
                @Override
                public ObservableValue<OrderItem.Type> call(TableColumn.CellDataFeatures<OrderItem, OrderItem.Type> item) {
                    return new PathProperty<>(item.getValue(), "type");
                }
            });

            tableView.getColumns().add(column);
        }

        {
            TableColumn<OrderItem, ProductInfo.Group> column = new TableColumn<>();
            column.setText("Group");
            column.setMinWidth(100);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderItem, ProductInfo.Group>, ObservableValue<ProductInfo.Group>>() {
                @Override
                public ObservableValue<ProductInfo.Group> call(TableColumn.CellDataFeatures<OrderItem, ProductInfo.Group> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.group");
                }
            });

            tableView.getColumns().add(column);
        }


        tableView.itemsProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object o2) {
            }
        });

        ToolBar toolBar = new ToolBar();
        ImageView imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/export-32x32.png"));
        Button exportOrder = new Button("", imageView);
        exportOrder.setContentDisplay(ContentDisplay.RIGHT);
        exportOrder.setTooltip(new Tooltip("Export to XLS"));
        exportOrder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // showCreateOrderDialog();
            }
        });
        toolBar.getItems().add(exportOrder);

        BorderPane content = new BorderPane();
        content.setCenter(tableView);
        content.setTop(toolBar);


        Tab tab = new Tab();
        tab.setText("Order Items");
        tab.setContent(content);
        tab.setClosable(false);
        return tab;
    }

    private TabPane createInvoiceView() {
        final TabPane tabPane = new TabPane();
        tabPane.setId("source-tabs");
        final Tab sourceTab = new Tab();
        sourceTab.setText("Invoice");

        sourceTab.setClosable(false);
        tabPane.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable ov) {
                /*if (tabPane.getSelectionModel().getSelectedItem() == sampleTab) {
                    sample.play();
                } else {
                    sample.stop();
                }*/
            }
        });
        tabPane.getTabs().addAll(createOrderItemTab(), sourceTab);

        ToolBar pdfToolBar = new ToolBar();
        ImageView imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/upload-32x32.png"));
        Button uploadInvoice = new Button("", imageView);
        uploadInvoice.setContentDisplay(ContentDisplay.RIGHT);
        uploadInvoice.setTooltip(new Tooltip("Export to XLS"));
        uploadInvoice.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Invoice PDF location");
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Pdf files (*.pdf)", "*.pdf");
                fileChooser.getExtensionFilters().add(extFilter);
                File selectedFile = fileChooser.showOpenDialog(getStage());

                if (selectedFile != null) {
                    try {
                        Task<Void> task = new CreateInvoicePdfTask(selectedFile.getName(), new FileInputStream(selectedFile));
                        progressIndicator.progressProperty().bind(task.progressProperty());
                        maskRegion.visibleProperty().bind(task.runningProperty());
                        progressIndicator.visibleProperty().bind(task.runningProperty());

                        new Thread(task).start();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        pdfToolBar.getItems().add(uploadInvoice);
        BorderPane pdfInvoicePane = new BorderPane();
        pdfInvoicePane.setTop(pdfToolBar);


        TableColumn<InvoicePdfTableItem, Boolean> checked = new TableColumn<>();

        checked.setMinWidth(50);
        checked.setCellValueFactory(new PropertyValueFactory<InvoicePdfTableItem, Boolean>("checked"));
        checked.setCellFactory(new Callback<TableColumn<InvoicePdfTableItem, Boolean>, TableCell<InvoicePdfTableItem, Boolean>>() {
            public TableCell<InvoicePdfTableItem, Boolean> call(TableColumn<InvoicePdfTableItem, Boolean> p) {
                return new CheckBoxTableCell<>();
            }
        });

        TableColumn<InvoicePdfTableItem, String> name = new TableColumn<>();

        name.setText("Name");
        name.setMinWidth(200);
        name.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoicePdfTableItem, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoicePdfTableItem, String> item) {
                return new PathProperty<>(item.getValue(), "invoicePdf.name");
            }
        });
        name.setCellFactory(TextFieldTableCell.<InvoicePdfTableItem>forTableColumn());
        name.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<InvoicePdfTableItem, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<InvoicePdfTableItem, String> t) {
                InvoicePdfTableItem tableItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                tableItem.setName(t.getNewValue());
                orderService.save(tableItem.getInvoicePdf());
            }
        });

        TableColumn<InvoicePdfTableItem, Double> priceColumn = new TableColumn<>();
        priceColumn.setText("Price");
        priceColumn.setMinWidth(60);
        priceColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoicePdfTableItem, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<InvoicePdfTableItem, Double> item) {
                return new PathProperty<>(item.getValue(), "invoicePdf.price");
            }
        });

        TableColumn createdDate = new TableColumn();

        createdDate.setText("Created Date");
        createdDate.setMinWidth(200);
        createdDate.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<InvoicePdfTableItem, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoicePdfTableItem, String> item) {
                        return new SimpleStringProperty(OrderListPage.DATE_FORMAT.format(item.getValue().getInvoicePdf().getCreatedDate()));
                    }
                });
        invoicePfdTableView = new TableView<>();
        invoicePfdTableView.getColumns().addAll(checked, name, priceColumn, createdDate);

        pdfInvoicePane.setCenter(invoicePfdTableView);
        SplitPane splitPane = new SplitPane();
        splitPane.setId("page-splitpane");
        splitPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        splitPane.setOrientation(Orientation.VERTICAL);


        rawInvoiceItemTableView = createRawInvoiceTableView();

        splitPane.getItems().addAll(pdfInvoicePane, rawInvoiceItemTableView);
        splitPane.setDividerPosition(0, 0.40);

        sourceTab.setContent(splitPane);


        return tabPane;
    }

    private TableView<RawInvoiceProductItem> createRawInvoiceTableView() {
        TableView<RawInvoiceProductItem> tableView = new TableView<>();

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
            tableView.getColumns().add(column);
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

            tableView.getColumns().add(column);
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

            tableView.getColumns().add(column);
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

            tableView.getColumns().add(column);
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

            tableView.getColumns().add(column);
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

            tableView.getColumns().add(column);
        }
        return tableView;
    }

    @Override
    public void onActive(Object... params) {
        currentOrder = (Order) params[0];
        tableView.setItems(FXCollections.observableArrayList(currentOrder.getOrderItems()));
        invoicePfdTableView.getItems().addAll(transform(currentOrder.getInvoicePdfs()));
        updateRawInvoiceTableView();
    }

    public static class InvoicePdfTableItem {
        private BooleanProperty checked;

        private InvoicePdf invoicePdf;

        public InvoicePdfTableItem(boolean checked, InvoicePdf invoicePdf) {
            setInvoicePdf(invoicePdf);
            this.checked = new SimpleBooleanProperty(checked);

            this.checked.addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                    System.out.println(InvoicePdfTableItem.this.invoicePdf.getName() + " invited: " + t1);
                }
            });
        }

        public InvoicePdf getInvoicePdf() {
            return invoicePdf;
        }

        public void setInvoicePdf(InvoicePdf invoicePdf) {
            this.invoicePdf = invoicePdf;
        }

        public void setName(String name) {
            invoicePdf.setName(name);
        }
    }


    private class CreateInvoicePdfTask extends Task<Void> {

        private String orderName;

        private InputStream is;

        public CreateInvoicePdfTask(String orderName, InputStream is) {
            this.orderName = orderName;
            this.is = is;
        }

        @Override
        protected Void call() throws InterruptedException {
            try {
                InvoicePdf entity = invoicePdfService.createInvoicePdf(orderName, is);
                orderService.save(entity);
                currentOrder.getInvoicePdfs().add(entity);
                orderService.save(currentOrder);
                invoicePfdTableView.getItems().clear();
                invoicePfdTableView.getItems().addAll(transform(currentOrder.getInvoicePdfs()));
                updateRawInvoiceTableView();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void updateRawInvoiceTableView() {
        List<InvoicePdf> checked = new ArrayList<>();
        for (InvoicePdfTableItem item : invoicePfdTableView.getItems())
            if (item.checked.getValue())
                checked.add(item.getInvoicePdf());

        List<RawInvoiceProductItem> forDisplay = new ArrayList<>();
        if (checked.isEmpty() && currentOrder.getInvoicePdfs() != null)
            checked.addAll(currentOrder.getInvoicePdfs());

        for (InvoicePdf invoicePdf : checked)
            forDisplay.addAll(invoicePdf.getProducts());

        rawInvoiceItemTableView.getItems().clear();
        rawInvoiceItemTableView.getItems().addAll(forDisplay);

    }

    private List<InvoicePdfTableItem> transform(List<InvoicePdf> entities) {
        List<InvoicePdfTableItem> result = new ArrayList<>();
        if (entities == null) return result;
        for (InvoicePdf entity : entities)
            result.add(new InvoicePdfTableItem(false, entity));
        return result;

    }
}
