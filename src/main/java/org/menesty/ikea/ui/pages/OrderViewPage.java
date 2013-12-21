package org.menesty.ikea.ui.pages;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.domain.StorageLack;
import org.menesty.ikea.exception.LoginIkeaException;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.*;
import org.menesty.ikea.ui.TaskProgress;
import org.menesty.ikea.ui.controls.component.InvoicePdfViewComponent;
import org.menesty.ikea.ui.controls.component.OrderItemViewComponent;
import org.menesty.ikea.ui.controls.component.RawInvoiceItemViewComponent;
import org.menesty.ikea.ui.controls.component.StorageLackItemViewComponent;
import org.menesty.ikea.ui.controls.dialog.EppDialog;
import org.menesty.ikea.ui.controls.dialog.IkeaUserFillProgressDialog;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;
import org.menesty.ikea.ui.controls.search.OrderItemSearchData;
import org.menesty.ikea.util.NumberUtil;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: Menesty
 * Date: 10/11/13
 * Time: 9:51 PM
 */
public class OrderViewPage extends BasePage {
    private OrderItemViewComponent orderItemViewComponent;

    private OrderService orderService;

    private InvoicePdfService invoicePdfService;

    private InvoiceService invoiceService;

    private CustomerOrder currentOrder;

    private InvoicePdfViewComponent invoicePdfViewComponent;

    private ProductDialog productEditDialog;

    private ProductService productService;

    private IkeaUserService ikeaUserService;

    private RawInvoiceItemViewComponent rawInvoiceItemViewComponent;

    private StorageLackItemViewComponent storageLackItemViewComponent;

    private Tab orderItemTab;

    public OrderViewPage() {
        super("CustomerOrder");

        orderService = new OrderService();
        invoicePdfService = new InvoicePdfService();
        invoiceService = new InvoiceService();
        productService = new ProductService();
        ikeaUserService = new IkeaUserService();
    }

    @Override
    public Node createView() {

        StackPane pane = createRoot();
        pane.getChildren().add(0, createInvoiceView());
        productEditDialog = new ProductDialog();
        return pane;
    }

    private Tab createStorageTab() {
        storageLackItemViewComponent = new StorageLackItemViewComponent() {

            @Override
            protected void onExportToIkea(List<StorageLack> items) {
                final List<StorageLack> storageLacks = new ArrayList<>(items);
                Iterator<StorageLack> iterator = storageLacks.iterator();
                while (iterator.hasNext()) {
                    if (!iterator.next().isExist())
                        iterator.remove();
                }

                final IkeaUserFillProgressDialog logDialog = new IkeaUserFillProgressDialog() {
                    @Override
                    public void onOk() {
                        hidePopupDialog();
                    }
                };
                OrderViewPage.this.showPopupDialog(logDialog);
                new Thread(new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            ikeaUserService.fillUser(currentOrder.getLackUser(), ProductInfo.Group.general(), ikeaUserService.groupItems(storageLacks), logDialog);
                        } catch (LoginIkeaException e) {
                            logDialog.addLog(e.getMessage());
                        } catch (IOException e) {
                            logDialog.addLog("Error happened during connection to IKEA site");
                        }

                        logDialog.done();
                        return null;
                    }
                }).start();

            }
        };
        final Tab tab = new Tab();
        tab.setText("Storage Lack");
        tab.setContent(storageLackItemViewComponent);
        tab.setClosable(false);
        tab.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if (tab.isSelected())
                    storageLackItemViewComponent.setItems(orderService.calculateOrderInvoiceDiff(currentOrder));

            }
        });
        return tab;
    }

    private Tab createOrderItemTab() {

        orderItemViewComponent = new OrderItemViewComponent(getStage()) {
            @Override
            protected void reloadProduct(OrderItem orderItem, final EventHandler<Event> onSucceeded) {
                Task<Void> task = new ProductFetch(orderItem);
                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent workerStateEvent) {
                        onSucceeded.handle(workerStateEvent);
                    }
                });
                runTask(task);
            }

            @Override
            protected void save(ProductInfo productInfo) {
                productService.save(productInfo);
            }

            @Override
            protected void hidePopupDialog() {
                OrderViewPage.this.hidePopupDialog();
            }

            @Override
            protected void showPopupDialog(ProductDialog productEditDialog) {
                OrderViewPage.this.showPopupDialog(productEditDialog);
            }

            @Override
            protected List<OrderItem> filter(OrderItemSearchData orderItemSearchForm) {
                return currentOrder.filterOrderItems(orderItemSearchForm);
            }

            @Override
            protected void onExport(String filePath) {
                runTask(new ExportOrderItemsTask(currentOrder, filePath));
            }

            @Override
            protected void onExportToIkea() {
                final IkeaUserFillProgressDialog logDialog = new IkeaUserFillProgressDialog() {
                    @Override
                    public void onOk() {
                        hidePopupDialog();
                    }
                };
                OrderViewPage.this.showPopupDialog(logDialog);
                try {
                    new Thread(new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            ikeaUserService.fillOrder(currentOrder, logDialog);
                            return null;
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                    hidePopupDialog();
                }
            }
        };


        Tab tab = new Tab("CustomerOrder Items");
        tab.setContent(orderItemViewComponent);
        tab.setClosable(false);
        return tab;
    }

    private TabPane createInvoiceView() {
        final TabPane tabPane = new TabPane();

        Tab invoiceTab = new Tab("Invoice");

        invoicePdfViewComponent = new InvoicePdfViewComponent(getStage()) {
            @Override
            public void onDelete(List<InvoicePdf> items) {
                orderService.remove(currentOrder, items, false);
                invoicePdfViewComponent.setItems(currentOrder.getInvoicePdfs());
                updateRawInvoiceTableView();
            }

            @Override
            public void onSave(InvoicePdf invoicePdf) {
                orderService.save(invoicePdf);
            }

            @Override
            public void onExport(List<File> files) {
                runTask(new CreateInvoicePdfTask(files));
            }

            @Override
            public void onSelect(InvoicePdf invoicePdf) {
                updateRawInvoiceTableView(invoicePdf);
            }
        };

        SplitPane splitPane = new SplitPane();
        splitPane.setId("page-splitpane");
        splitPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        splitPane.setOrientation(Orientation.VERTICAL);

        rawInvoiceItemViewComponent = new RawInvoiceItemViewComponent(getStage()) {
            @Override
            public void onExport(List<RawInvoiceProductItem> items, String path) {
                invoiceService.exportToXls(items, path);
            }

            @Override
            public void onRowDoubleClick(final TableRow<RawInvoiceProductItem> row) {
                showPopupDialog(productEditDialog);
                productEditDialog.bind(row.getItem().getProductInfo(), new EntityDialogCallback<ProductInfo>() {
                    @Override
                    public void onSave(ProductInfo productInfo, Object[] params) {
                        productService.save(productInfo);
                        if (!(Boolean) params[0])
                            hidePopupDialog();
                        row.setItem(null);
                    }

                    @Override
                    public void onCancel() {
                        hidePopupDialog();
                    }
                });
            }

            @Override
            public void exportToEpp(List<InvoiceItem> items, BigDecimal price) {
                EppDialog dialog = new EppDialog(getStage()) {
                    @Override
                    public void export(List<InvoiceItem> items, String path) {
                        invoiceService.exportToEpp(items, path);
                        hidePopupDialog();
                    }

                    @Override
                    public void onCancel() {
                        hidePopupDialog();
                    }
                };
                dialog.setItems(items, price);
                showPopupDialog(dialog);

            }
        };


        splitPane.getItems().addAll(invoicePdfViewComponent, rawInvoiceItemViewComponent);
        splitPane.setDividerPosition(0, 0.40);

        invoiceTab.setClosable(false);
        invoiceTab.setContent(splitPane);

        tabPane.getTabs().addAll(orderItemTab = createOrderItemTab(), invoiceTab, createStorageTab());
        return tabPane;
    }


    @Override
    public void onActive(Object... params) {
        currentOrder = (CustomerOrder) params[0];
        orderItemViewComponent.setItems(currentOrder.getOrderItems());
        invoicePdfViewComponent.setItems(currentOrder.getInvoicePdfs());
        updateRawInvoiceTableView();

        orderItemViewComponent.disableIkeaExport(currentOrder.getGeneralUser() == null || currentOrder.getComboUser() == null);
        storageLackItemViewComponent.disableIkeaExport(currentOrder.getLackUser() == null);

        orderItemTab.setText(currentOrder.getName() + " - " + orderItemTab.getText());
        rawInvoiceItemViewComponent.setEppPrefix(((int) NumberUtil.parse(currentOrder.getName())) + "");
    }


    class CreateInvoicePdfTask extends Task<Void> {

        private final List<File> files;


        public CreateInvoicePdfTask(List<File> files) {
            this.files = files;
        }

        @Override
        protected Void call() throws Exception {
            try {
                List<InvoicePdf> entities = invoicePdfService.createInvoicePdf(files);
                currentOrder.addInvoicePdfs(entities);
                orderService.save(currentOrder);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        invoicePdfViewComponent.setItems(currentOrder.getInvoicePdfs());
                        updateRawInvoiceTableView();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class ProductFetch extends Task<Void> {

        private final OrderItem orderItem;

        public ProductFetch(OrderItem orderItem) {
            this.orderItem = orderItem;
        }

        @Override
        protected Void call() throws Exception {
            ProductInfo productInfo = productService.loadOrCreate(orderItem.getArtNumber());
            if (productInfo == null)
                orderItem.increaseTryCount();
            else {
                orderItem.setProductInfo(productInfo);
                orderItem.setInvalidFetch(false);
                productService.save(orderItem);
            }
            return null;
        }
    }

    class ExportOrderItemsTask extends Task<Void> {

        private CustomerOrder order;

        private String fileName;

        public ExportOrderItemsTask(CustomerOrder order, String fileName) {
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
        updateRawInvoiceTableView(invoicePdfViewComponent.getSelected());
    }

    private void updateRawInvoiceTableView(InvoicePdf selected) {
        List<InvoicePdf> selectedItems = new ArrayList<>();
        List<RawInvoiceProductItem> forDisplay = new ArrayList<>();

        if (selected == null && currentOrder.getInvoicePdfs() != null)
            selectedItems.addAll(currentOrder.getInvoicePdfs());
        else
            selectedItems.add(selected);

        for (InvoicePdf invoicePdf : selectedItems)
            forDisplay.addAll(invoicePdf.getProducts());

        rawInvoiceItemViewComponent.setItems(forDisplay);
    }
}