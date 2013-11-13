package org.menesty.ikea.ui.pages;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.Order;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.*;
import org.menesty.ikea.ui.TaskProgress;
import org.menesty.ikea.ui.controls.InvoicePdfViewComponent;
import org.menesty.ikea.ui.controls.OrderItemViewComponent;
import org.menesty.ikea.ui.controls.RawInvoiceItemViewComponent;
import org.menesty.ikea.ui.controls.dialog.EppDialog;
import org.menesty.ikea.ui.controls.dialog.IkeaUserFillProgressDialog;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;
import org.menesty.ikea.ui.controls.search.OrderItemSearchData;

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
    private OrderItemViewComponent orderItemViewComponent;

    private OrderService orderService;

    private InvoicePdfService invoicePdfService;

    private InvoiceService invoiceService;

    private Order currentOrder;

    private InvoicePdfViewComponent invoicePdfViewComponent;

    private ProductDialog productEditDialog;

    private ProductService productService;

    private IkeaUserService ikeaUserService;

    private RawInvoiceItemViewComponent rawInvoiceItemViewComponent;

    public OrderViewPage() {
        super("Order");
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

    private Tab createOrderItemTab() {

        orderItemViewComponent = new OrderItemViewComponent(getStage()) {
            @Override
            public void save(ProductInfo productInfo) {
                productService.save(productInfo);
            }

            @Override
            public void hidePopupDialog() {
                OrderViewPage.this.hidePopupDialog();
            }

            @Override
            public void showPopupDialog(ProductDialog productEditDialog) {
                OrderViewPage.this.showPopupDialog(productEditDialog);
            }

            @Override
            public List<OrderItem> filter(OrderItemSearchData orderItemSearchForm) {
                return currentOrder.filterOrderItems(orderItemSearchForm);
            }

            @Override
            public void onExport(String filePath) {
                runTask(new ExportOrderItemsTask(currentOrder, filePath));
            }

            @Override
            public void onExportToIkea() {
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


        Tab tab = new Tab();
        tab.setText("Order Items");
        tab.setContent(orderItemViewComponent);
        tab.setClosable(false);
        return tab;
    }

    private TabPane createInvoiceView() {
        final TabPane tabPane = new TabPane();
        tabPane.setId("source-tabs");
        final Tab sourceTab = new Tab();
        sourceTab.setText("Invoice");

        sourceTab.setClosable(false);
        tabPane.getTabs().addAll(createOrderItemTab(), sourceTab);

        invoicePdfViewComponent = new InvoicePdfViewComponent(getStage()) {
            @Override
            public void onSave(InvoicePdf invoicePdf) {
                orderService.save(invoicePdf);
            }

            @Override
            public void onExport(String fileName, String filePath) {
                try {
                    runTask(new CreateInvoicePdfTask(fileName, new FileInputStream(filePath)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSelect(List<InvoicePdf> invoicePdfs) {
                updateRawInvoiceTableView(invoicePdfs);
            }
        };

        SplitPane splitPane = new SplitPane();
        splitPane.setId("page-splitpane");
        splitPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        splitPane.setOrientation(Orientation.VERTICAL);

        rawInvoiceItemViewComponent = new RawInvoiceItemViewComponent(getStage()) {
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
            public void exportToEpp(List<InvoiceItem> items, double price) {
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

        sourceTab.setContent(splitPane);


        return tabPane;
    }


    @Override
    public void onActive(Object... params) {
        currentOrder = (Order) params[0];
        orderItemViewComponent.setItems(currentOrder.getOrderItems());
        invoicePdfViewComponent.setItems(currentOrder.getInvoicePdfs());
        updateRawInvoiceTableView();

        orderItemViewComponent.disableIkeaExport(currentOrder.getGeneralUser() == null || currentOrder.getComboUser() == null);
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
        updateRawInvoiceTableView(invoicePdfViewComponent.getSelected());
    }

    private void updateRawInvoiceTableView(List<InvoicePdf> selected) {

        List<RawInvoiceProductItem> forDisplay = new ArrayList<>();

        if (selected.isEmpty() && currentOrder.getInvoicePdfs() != null)
            selected.addAll(currentOrder.getInvoicePdfs());

        for (InvoicePdf invoicePdf : selected)
            forDisplay.addAll(invoicePdf.getProducts());

        rawInvoiceItemViewComponent.setItems(forDisplay);
    }
}