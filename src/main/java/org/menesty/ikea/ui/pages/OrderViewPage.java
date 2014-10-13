package org.menesty.ikea.ui.pages;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.apache.commons.lang.StringUtils;
import org.menesty.ikea.db.DatabaseService;
import org.menesty.ikea.domain.*;
import org.menesty.ikea.exception.LoginIkeaException;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.service.task.InvoicePdfSyncService;
import org.menesty.ikea.service.task.OrderInvoiceSyncService;
import org.menesty.ikea.ui.CallBack;
import org.menesty.ikea.ui.TaskProgress;
import org.menesty.ikea.ui.controls.component.*;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.dialog.IkeaUserFillProgressDialog;
import org.menesty.ikea.ui.controls.search.OrderItemSearchData;
import org.menesty.ikea.util.NumberUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * User: Menesty
 * Date: 10/11/13
 * Time: 9:51 PM
 */
public class OrderViewPage extends BasePage {
    private OrderItemViewComponent orderItemViewComponent;

    private CustomerOrder currentOrder;

    private StorageLackItemViewComponent storageLackItemViewComponent;

    private OrderInvoiceSyncService orderInvoiceSyncService;

    private OrderData orderData;

    private LoadService loadService;

    private StoreLackService storeLackService;

    private StoreLackComboService storeLackComboService;

    private StorageLackComboComponent storageLackComboComponent;

    private RawInvoiceItemSearchComponent rawInvoiceItemSearchComponent;

    private InvoicePdfSyncService invoicePdfSyncService;

    private Tab storageTab;
    private Tab storageComboTab;
    private Tab orderItemTab;
    private InvoiceComponent invoiceComponent;
    private TabPane tabPane;

    public OrderViewPage() {
        super("CustomerOrder");
    }

    @Override
    protected void initialize() {
        orderInvoiceSyncService = new OrderInvoiceSyncService();
        orderInvoiceSyncService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<Boolean>() {
            @Override
            public void onSucceeded(Boolean value) {
                if (value) {
                    ServiceFacade.getInvoicePdfService().updateSyncBy(currentOrder);

                    for (InvoicePdf pdf : orderData.invoicePdfs)
                        pdf.setSync(true);
                }
            }
        });

        loadService = new LoadService();

        loadService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<OrderData>() {
            @Override
            public void onSucceeded(final OrderData value) {
                OrderViewPage.this.orderData = value;

                invoiceComponent.setOrderData(orderData);
                orderItemViewComponent.setItems(orderData.orderItems);

                updateInvoiceRawSearchTab();
            }
        });

        storeLackService = new StoreLackService();

        storeLackService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<List<StorageLack>>() {
            @Override
            public void onSucceeded(List<StorageLack> value) {
                storageLackItemViewComponent.setItems(value);
            }
        });

        storeLackComboService = new StoreLackComboService();

        storeLackComboService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<List<StorageComboLack>>() {
            @Override
            public void onSucceeded(List<StorageComboLack> value) {
                storageLackComboComponent.setItems(value);
            }
        });

        invoicePdfSyncService = new InvoicePdfSyncService();
        invoicePdfSyncService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<Boolean>() {
            @Override
            public void onSucceeded(Boolean value) {
                if (value) {
                    InvoicePdf invoicePdf = invoicePdfSyncService.getInvoice();
                    invoicePdf.setSync(true);
                    ServiceFacade.getInvoicePdfService().save(invoicePdf);
                    invoiceComponent.update(invoicePdf);
                }
            }
        });
    }

    private void updateInvoiceRawSearchTab() {
        List<RawInvoiceProductItem> items = new ArrayList<>();

        for (InvoicePdf pdf : orderData.invoicePdfs)
            items.addAll(pdf.getProducts());

        rawInvoiceItemSearchComponent.setItems(items);
    }

    @Override
    public Node createView() {

        return wrap(createInvoiceView());
    }

    private Tab createStorageComboTab() {
        final Tab tab = new Tab("Storage Combo Lack");
        tab.setClosable(false);

        storageLackComboComponent = new StorageLackComboComponent();
        tab.setContent(storageLackComboComponent);
        tab.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if (tab.isSelected()) {
                    loadingPane.bindTask(storeLackComboService);
                    storeLackComboService.restart();
                }
            }
        });

        return tab;
    }

    private Tab createStorageTab() {
        storageLackItemViewComponent = new StorageLackItemViewComponent() {

            @Override
            protected void onExportToIkea(List<StorageLack> items) {
                final List<StorageLack> storageLacks = new ArrayList<>(items);
                Iterator<StorageLack> iterator = storageLacks.iterator();

                while (iterator.hasNext())
                    if (!iterator.next().isExist())
                        iterator.remove();

                final IkeaUserFillProgressDialog logDialog = new IkeaUserFillProgressDialog(getStage()) {
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
                            ServiceFacade.getIkeaUserService().fillUser(
                                    currentOrder.getLackUser(), ProductInfo.Group.general(),
                                    ServiceFacade.getIkeaUserService().groupItems(storageLacks), logDialog);

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

        final Tab tab = new Tab("Storage Lack");
        tab.setContent(storageLackItemViewComponent);
        tab.setClosable(false);
        tab.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if (tab.isSelected()) {
                    loadingPane.bindTask(storeLackService);
                    storeLackService.restart();
                }
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
                ServiceFacade.getProductService().save(productInfo);
            }

            @Override
            protected void save(OrderItem orderItem) {
                if (orderItem.getId() == null) {
                    orderItem.setCustomerOrder(currentOrder);

                    for (OrderItem item : orderData.orderItems)
                        if (item.getArtNumber().equals(orderItem.getArtNumber())) {
                            item.addCount(orderItem.getCount());
                            orderItem = item;
                            break;
                        }
                }

                ServiceFacade.getOrderService().save(orderItem);

                if (!orderData.orderItems.contains(orderItem))
                    orderData.orderItems.add(orderItem);

                orderItemViewComponent.setItems(orderData.orderItems);
            }

            @Override
            protected void hidePopupDialog() {
                OrderViewPage.this.hidePopupDialog();
            }

            @Override
            protected void showPopupDialog(BaseDialog productEditDialog) {
                OrderViewPage.this.showPopupDialog(productEditDialog);
            }

            @Override
            protected List<OrderItem> filter(OrderItemSearchData orderItemSearchForm) {
                return filterOrderItems(orderItemSearchForm);
            }

            @Override
            protected void onExport(String filePath) {
                runTask(new ExportOrderItemsTask(currentOrder, filePath));
            }

            @Override
            protected void onExportToIkea() {
                final IkeaUserFillProgressDialog logDialog = new IkeaUserFillProgressDialog(getStage()) {
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
                            ServiceFacade.getNewOrderFillService().fillOrder(currentOrder, logDialog);
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

    public List<OrderItem> filterOrderItems(OrderItemSearchData orderItemSearchForm) {
        List<OrderItem> result = new ArrayList<>();

        for (OrderItem orderItem : orderData.orderItems) {

            ProductInfo productInfo = orderItem.getProductInfo();

            if (orderItemSearchForm.type != null && orderItemSearchForm.type != orderItem.getType())
                continue;

            if (StringUtils.isNotBlank(orderItemSearchForm.artNumber) && !orderItem.getArtNumber().contains(ProductInfo.cleanProductId(orderItemSearchForm.artNumber)))
                continue;

            if (orderItemSearchForm.productGroup != null &&
                    (productInfo == null || !orderItemSearchForm.productGroup.equals(productInfo.getGroup())))
                continue;

            if (orderItemSearchForm.pum &&
                    (OrderItem.Type.Na == orderItem.getType() ||
                            (productInfo != null && orderItem.getPrice() == productInfo.getPrice())
                    ))
                continue;

            if (productInfo != null) {
                if (orderItemSearchForm.gei && !(productInfo.getPackageInfo().getWeight() > 3000 ||
                        productInfo.getPackageInfo().getLength() > 450 || productInfo.getPackageInfo().getWidth() > 450 || productInfo.getPackageInfo().getHeight() > 450
                ))
                    continue;

                if (orderItemSearchForm.ufd && productInfo.getPackageInfo().hasAllSize())
                    continue;
            }

            result.add(orderItem);
        }

        return result;
    }

    private Tab createInvoiceRawItemSearchTab() {
        Tab tab = new Tab("Invoice Item Search");
        tab.setClosable(false);

        rawInvoiceItemSearchComponent = new RawInvoiceItemSearchComponent();
        tab.setContent(rawInvoiceItemSearchComponent);

        return tab;
    }

    private TabPane createInvoiceView() {
        tabPane = new TabPane();

        Tab invoiceTab = new Tab("Invoice");
        invoiceTab.setClosable(false);
        invoiceTab.setContent(invoiceComponent = new InvoiceComponent(getDialogSupport()) {
            @Override
            protected CustomerOrder getCustomerOrder() {
                return currentOrder;
            }
        });
        invoiceComponent.setListener(new InvoiceComponent.InvoiceComponentListener() {
            @Override
            public void onSync(boolean clear) {
                orderInvoiceSyncService.setCustomerOrder(currentOrder);
                orderInvoiceSyncService.setClear(clear);

                loadingPane.bindTask(orderInvoiceSyncService);
                orderInvoiceSyncService.restart();
            }

            @Override
            public void onSave(InvoicePdf invoicePdf) {
                loadingPane.bindTask(loadService);
                loadService.restart();
            }

            @Override
            public void onImport(List<File> files, CallBack<List<RawInvoiceProductItem>> callBack) {
                runTask(new CreateInvoicePdfTask(files, callBack));
            }

            @Override
            public void onUpload(InvoicePdf invoicePdf) {
                loadingPane.bindTask(invoicePdfSyncService);
                invoicePdfSyncService.setInvoice(invoicePdf);
                invoicePdfSyncService.restart();
            }

            @Override
            public void onRawItemsUpdate() {
                updateInvoiceRawSearchTab();
            }
        });

        tabPane.getTabs().addAll(orderItemTab = createOrderItemTab(), invoiceTab,
                createInvoiceRawItemSearchTab(), storageTab = createStorageTab(),
                storageComboTab = createStorageComboTab());

        return tabPane;
    }


    @Override
    public void onActive(Object... params) {
        currentOrder = (CustomerOrder) params[0];

        loadingPane.bindTask(loadService);
        loadService.restart();

        orderItemViewComponent.disableIkeaExport(currentOrder.getUsers().size() == 0);
        storageLackItemViewComponent.disableIkeaExport(currentOrder.getLackUser() == null);

        orderItemTab.setText(currentOrder.getName() + " - " + orderItemTab.getText());
        invoiceComponent.setEppPrefix(((int) NumberUtil.parse(currentOrder.getName())) + "");
        invoiceComponent.updateView();

        if (currentOrder.isSynthetic())
            tabPane.getTabs().removeAll(orderItemTab, storageTab, storageComboTab);
    }


    class CreateInvoicePdfTask extends Task<Void> {

        private final List<File> files;
        private final CallBack<List<RawInvoiceProductItem>> callBack;

        public CreateInvoicePdfTask(List<File> files, CallBack<List<RawInvoiceProductItem>> callBack) {
            this.files = files;
            this.callBack = callBack;
        }

        @Override
        protected Void call() throws Exception {
            try {
                List<InvoicePdf> entities = ServiceFacade.getInvoicePdfService().createInvoicePdf(currentOrder, files, callBack);
                orderData.invoicePdfs.addAll(entities);
                ServiceFacade.getOrderService().save(currentOrder);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        invoiceComponent.refreshInvoicePdfs();
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
            return DatabaseService.runInTransaction(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ProductInfo productInfo = ServiceFacade.getProductService().loadOrCreate(orderItem.getArtNumber());

                    if (productInfo == null)
                        orderItem.increaseTryCount();
                    else {
                        orderItem.setProductInfo(productInfo);
                        orderItem.setInvalidFetch(false);
                        ServiceFacade.getProductService().save(orderItem);
                    }

                    return null;
                }
            });
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
                ServiceFacade.getOrderService().exportToXls(order, fileName, new TaskProgress() {
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


    class LoadService extends AbstractAsyncService<OrderData> {
        @Override
        protected Task<OrderData> createTask() {
            return new Task<OrderData>() {
                @Override
                protected OrderData call() throws Exception {
                    return DatabaseService.runInTransaction(new Callable<OrderData>() {
                        @Override
                        public OrderData call() throws Exception {
                            OrderData orderData = new OrderData();
                            orderData.invoicePdfs = ServiceFacade.getInvoicePdfService().loadBy(currentOrder);
                            orderData.orderItems = ServiceFacade.getOrderItemService().loadBy(currentOrder);

                            return orderData;
                        }
                    });
                }
            };
        }
    }

    class StoreLackService extends AbstractAsyncService<List<StorageLack>> {
        @Override
        protected Task<List<StorageLack>> createTask() {
            return new Task<List<StorageLack>>() {
                @Override
                protected List<StorageLack> call() throws Exception {
                    return ServiceFacade.getOrderService().calculateOrderInvoiceDiffWithoutCombo(currentOrder, Collections.unmodifiableList(orderData.orderItems));
                }
            };
        }
    }

    class StoreLackComboService extends AbstractAsyncService<List<StorageComboLack>> {

        @Override
        protected Task<List<StorageComboLack>> createTask() {
            return new Task<List<StorageComboLack>>() {
                @Override
                protected List<StorageComboLack> call() throws Exception {
                    try {
                        return ServiceFacade.getOrderService().calculateOrderInvoiceDiffCombo(currentOrder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            };
        }
    }

    public static class OrderData {
        public List<OrderItem> orderItems;
        public List<InvoicePdf> invoicePdfs;
    }
}