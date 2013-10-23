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
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.Order;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.InvoicePdfService;
import org.menesty.ikea.service.InvoiceService;
import org.menesty.ikea.service.OrderService;
import org.menesty.ikea.service.ProductService;
import org.menesty.ikea.ui.TaskProgress;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;
import org.menesty.ikea.ui.controls.table.OrderItemTableView;
import org.menesty.ikea.ui.controls.table.RawInvoiceTableView;

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

    private OrderItemTableView tableView;

    private OrderService orderService;

    private InvoicePdfService invoicePdfService;

    private InvoiceService invoiceService;

    private Order currentOrder;

    private RawInvoiceTableView rawInvoiceItemTableView;

    private TableView<InvoicePdfTableItem> invoicePfdTableView;

    private ProductDialog productEditDialog;

    private ProductService productService;

    public OrderViewPage() {
        super("Order");
        orderService = new OrderService();
        invoicePdfService = new InvoicePdfService();
        invoiceService = new InvoiceService();
        productService = new ProductService();
    }

    @Override
    public Node createView() {

        StackPane pane = createRoot();
        pane.getChildren().add(0, createInvoiceView());
        productEditDialog = new ProductDialog();
        return pane;
    }

    private Tab createOrderItemTab() {
        tableView = new OrderItemTableView() {
            @Override
            public void onRowDoubleClick(final TableRow<OrderItem> row) {
                showPopupDialog(productEditDialog);
                productEditDialog.bind(row.getItem().getProductInfo(), new DialogCallback() {
                    @Override
                    public void onSave(ProductInfo productInfo, boolean isCombo) {
                        productService.save(productInfo);
                        if (!isCombo)
                            hidePopupDialog();

                        row.setItem(null);
                    }

                    @Override
                    public void onCancel() {
                        hidePopupDialog();
                    }
                });
            }
        };


        tableView.itemsProperty().addListener(new ChangeListener<ObservableList<OrderItem>>() {

            @Override
            public void changed(ObservableValue<? extends ObservableList<OrderItem>> observableValue, ObservableList<OrderItem> orderItems, ObservableList<OrderItem> orderItems2) {
                System.out.print("items update");
            }
        });

        ToolBar toolBar = new ToolBar();
        ImageView imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/export-32x32.png"));
        Button exportOrder = new Button("", imageView);
        exportOrder.setContentDisplay(ContentDisplay.RIGHT);
        exportOrder.setTooltip(new Tooltip("Export to XLS"));
        exportOrder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();

                //Set extension filter
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Zip file (*.zip)", "*.zip");
                fileChooser.getExtensionFilters().add(extFilter);

                //Show save file dialog
                File file = fileChooser.showSaveDialog(getStage());

                if (file != null) {
                    runTask(new ExportOrderItemsTask(currentOrder, file.getAbsolutePath()));
                }
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
        uploadInvoice.setTooltip(new Tooltip("Upload Invoice PDF"));
        uploadInvoice.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Invoice PDF location");
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Pdf files (*.pdf)", "*.pdf");
                fileChooser.getExtensionFilters().add(extFilter);
                File selectedFile = fileChooser.showOpenDialog(getStage());

                if (selectedFile != null) {
                    try {
                        runTask(new CreateInvoicePdfTask(selectedFile.getName(), new FileInputStream(selectedFile)));
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

        checked.setMaxWidth(40);
        checked.setResizable(false);
        checked.setCellValueFactory(new PropertyValueFactory<InvoicePdfTableItem, Boolean>("checked"));
        checked.setCellFactory(new Callback<TableColumn<InvoicePdfTableItem, Boolean>, TableCell<InvoicePdfTableItem, Boolean>>() {
            public TableCell<InvoicePdfTableItem, Boolean> call(TableColumn<InvoicePdfTableItem, Boolean> p) {
                CheckBoxTableCell<InvoicePdfTableItem, Boolean> checkBoxTableCell = new CheckBoxTableCell<>();
                checkBoxTableCell.setAlignment(Pos.CENTER);
                return checkBoxTableCell;
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

        TableColumn<InvoicePdfTableItem, String> createdDate = new TableColumn();

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
        invoicePfdTableView.setEditable(true);
        invoicePfdTableView.getColumns().addAll(checked, name, priceColumn, createdDate);

        pdfInvoicePane.setCenter(invoicePfdTableView);
        SplitPane splitPane = new SplitPane();
        splitPane.setId("page-splitpane");
        splitPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        splitPane.setOrientation(Orientation.VERTICAL);


        BorderPane rawInvoicePane = new BorderPane();
        ToolBar rawInvoiceControl = new ToolBar();
        imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/export-32x32.png"));
        Button exportEppButton = new Button("", imageView);
        exportEppButton.setContentDisplay(ContentDisplay.RIGHT);
        exportEppButton.setTooltip(new Tooltip("Export to EPP"));
        exportEppButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Epp location");
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Epp file (*.epp)", "*.epp");
                fileChooser.getExtensionFilters().add(extFilter);
                File selectedFile = fileChooser.showSaveDialog(getStage());

                if (selectedFile != null)
                    invoiceService.exportToEpp(rawInvoiceItemTableView.getItems(), selectedFile.getAbsolutePath());

            }
        });
        rawInvoiceControl.getItems().add(exportEppButton);


        rawInvoiceItemTableView = new RawInvoiceTableView() {
            @Override
            public void onRowDoubleClick(final TableRow<RawInvoiceProductItem> row) {
                showPopupDialog(productEditDialog);
                productEditDialog.bind(row.getItem().getProductInfo(), new DialogCallback() {
                    @Override
                    public void onSave(ProductInfo productInfo, boolean isCombo) {
                        productService.save(productInfo);
                        if (!isCombo)
                            hidePopupDialog();
                        row.setItem(null);
                    }

                    @Override
                    public void onCancel() {
                        hidePopupDialog();
                    }
                });
            }
        };

        rawInvoicePane.setTop(rawInvoiceControl);
        rawInvoicePane.setCenter(rawInvoiceItemTableView);


        splitPane.getItems().addAll(pdfInvoicePane, rawInvoicePane);
        splitPane.setDividerPosition(0, 0.40);

        sourceTab.setContent(splitPane);


        return tabPane;
    }


    @Override
    public void onActive(Object... params) {
        currentOrder = (Order) params[0];
        tableView.setItems(FXCollections.observableArrayList(currentOrder.getOrderItems()));
        invoicePfdTableView.getItems().addAll(transform(currentOrder.getInvoicePdfs()));
        updateRawInvoiceTableView();
    }

    public class InvoicePdfTableItem {
        private BooleanProperty checked;

        private InvoicePdf invoicePdf;

        public InvoicePdfTableItem(boolean checked, InvoicePdf invoicePdf) {
            setInvoicePdf(invoicePdf);
            this.checked = new SimpleBooleanProperty(checked);

            this.checked.addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                    System.out.println(InvoicePdfTableItem.this.invoicePdf.getName() + " invited: " + t1);
                    OrderViewPage.this.updateRawInvoiceTableView();
                }
            });
        }

        public BooleanProperty checkedProperty() {
            return checked;
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
        protected Void call() throws Exception {
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

    private class ExportOrderItemsTask extends Task<Void> {
        private Order order;
        private String fileName;

        public ExportOrderItemsTask(Order order, String fileName) {
            this.order = order;
            this.fileName = fileName;
        }

        @Override
        protected Void call() throws Exception {
            try {
                orderService.exportToXls(order, fileName, new TaskProgress() {
                    @Override
                    public void updateProgress(long l, long l1) {
                        ExportOrderItemsTask.this.updateProgress(l, l1);
                    }
                });
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
