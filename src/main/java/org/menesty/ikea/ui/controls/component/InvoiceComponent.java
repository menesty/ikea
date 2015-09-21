package org.menesty.ikea.ui.controls.component;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableRow;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.db.DatabaseService;
import org.menesty.ikea.domain.*;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.OrderItemService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.CallBack;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.OrderViewPage;
import org.menesty.ikea.util.NumberUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Menesty on
 * 8/13/14.
 * 7:23.
 */
public abstract class InvoiceComponent extends BorderPane {

    private final InvoicePdfViewComponent invoicePdfViewComponent;
    private final RawInvoiceItemViewComponent rawInvoiceItemViewComponent;
    private final EppViewComponent eppViewComponent;
    private final SplitPane invoiceSplitPane;
    private OrderViewPage.OrderData orderData;
    private ProductDialog productEditDialog;
    private InvoiceComponentListener listener;

    public InvoiceComponent(final DialogSupport dialogSupport) {
        final CallBack<List<RawInvoiceProductItem>> importCallBack = data -> {
            String artSufix = getCustomerOrder().getName();
            for (RawInvoiceProductItem item : data) {
                String prefix = item.getProductInfo() != null ? "IKEA" : "SYN";

                if (item.getProductInfo() == null) {
                    InvoiceItem invoiceItem = InvoiceItem.get(item.getOriginalArtNumber(), prefix, artSufix,
                            item.getName(), item.getName(),
                            item.getPrice(), item.getIntWat(), "", 1, item.getCount(), 1, 1);

                    invoiceItem.invoicePdf = item.invoicePdf;

                    ServiceFacade.getInvoiceItemService().save(invoiceItem);
                } else {
                    String artPrefix = item.invoicePdf.customerOrder.getName();
                    List<InvoiceItem> items = InvoiceItem.get(item.getProductInfo(), artPrefix, item.getCount());

                    for (InvoiceItem invoiceItem : items) {
                        invoiceItem.invoicePdf = item.invoicePdf;
                    }

                    ServiceFacade.getInvoiceItemService().save(items);
                }
            }
        };

        productEditDialog = new ProductDialog(dialogSupport.getStage());
        invoicePdfViewComponent = new InvoicePdfViewComponent(dialogSupport) {
            @Override
            protected void onFake() {
                DatabaseService.runInTransaction(() -> {
                    InvoicePdf invoicePdf = new InvoicePdf();
                    invoicePdf.setInvoiceNumber("All Order #" + getCustomerOrder().getId());
                    invoicePdf.setName("All order items #" + getCustomerOrder().getId());
                    invoicePdf.customerOrder = getCustomerOrder();

                    ServiceFacade.getOrderItemService().save(invoicePdf);


                    List<OrderItem> orderItems = ServiceFacade.getOrderItemService().loadBy(getCustomerOrder());
                    List<RawInvoiceProductItem> rawInvoiceProductItems = new ArrayList<>();

                    for (OrderItem orderItem : orderItems) {
                        if (orderItem.getProductInfo() == null) {
                            continue;
                        }

                        RawInvoiceProductItem rawOrderItem = new RawInvoiceProductItem(invoicePdf);
                        rawOrderItem.setProductInfo(orderItem.getProductInfo());
                        rawOrderItem.setSynthetic(false);
                        rawOrderItem.setPrice(orderItem.getProductInfo().getPrice());
                        rawOrderItem.setCount(orderItem.getCount());
                        rawOrderItem.setOriginalArtNumber(orderItem.getProductInfo().getOriginalArtNum());
                        rawOrderItem.setWat("23,00%");
                        rawOrderItem.setName(orderItem.getProductInfo().getShortName() != null ? orderItem.getProductInfo().getShortName().replaceAll("-", "")
                                : orderItem.getProductInfo().getName());

                        ServiceFacade.getOrderItemService().save(rawOrderItem);
                        rawInvoiceProductItems.add(rawOrderItem);
                    }
                    invoicePdf.setProducts(rawInvoiceProductItems);
                    importCallBack.onResult(rawInvoiceProductItems);
                    orderData.invoicePdfs.add(invoicePdf);

                    Platform.runLater(() -> refreshInvoicePdfs());
                    return null;
                });
            }

            @Override
            protected void onSync(boolean clear) {
                listener.onSync(clear);
            }

            @Override
            protected CustomerOrder getCustomerOrder() {
                return InvoiceComponent.this.getCustomerOrder();
            }

            @Override
            public void onDelete(List<InvoicePdf> items) {
                ServiceFacade.getInvoicePdfService().removeAll(items);
                orderData.invoicePdfs.removeAll(items);
                invoicePdfViewComponent.setItems(orderData.invoicePdfs);
                updateRawInvoiceTableView();
            }

            @Override
            public void onSave(InvoicePdf invoicePdf) {
                ServiceFacade.getOrderService().save(invoicePdf);
                listener.onSave(invoicePdf);
            }

            @Override
            public void onImport(List<File> files) {
                listener.onImport(files, importCallBack);
            }

            @Override
            public void onUpload(InvoicePdf invoicePdf) {
                listener.onUpload(invoicePdf);

            }

            @Override
            public void onSelect(InvoicePdf invoicePdf) {
                updateRawInvoiceTableView(invoicePdf);
                rawInvoiceItemViewComponent.disableControls(false);
            }
        };

        invoiceSplitPane = new SplitPane();
        invoiceSplitPane.setId("page-splitpane");
        invoiceSplitPane.setOrientation(Orientation.VERTICAL);

        rawInvoiceItemViewComponent = new RawInvoiceItemViewComponent(dialogSupport) {
            @Override
            protected String getOrderName() {
                return getCustomerOrder().getName();
            }

            @Override
            protected void exportEpp(String filePath) {
                ServiceFacade.getInvoiceService().exportToEpp(getCustomerOrder(), filePath);
            }

            @Override
            protected void onSave(RawInvoiceProductItem item) {
                ServiceFacade.getInvoicePdfService().save(item);
                importCallBack.onResult(Arrays.asList(item));
                //reload InvoicePdf items
                ServiceFacade.getInvoicePdfService().reloadProducts(getInvoicePdf());
                updateRawInvoiceTableView(getInvoicePdf());
                listener.onRawItemsUpdate();
            }

            @Override
            protected InvoicePdf getInvoicePdf() {
                return invoicePdfViewComponent.getSelected();
            }

            @Override
            public void onExport(List<RawInvoiceProductItem> items, String path) {
                ServiceFacade.getInvoiceService().exportToXls(items, path);
            }

            @Override
            public void onRowDoubleClick(final TableRow<RawInvoiceProductItem> row) {
                dialogSupport.showPopupDialog(productEditDialog);
                productEditDialog.bind(row.getItem().getProductInfo(), new EntityDialogCallback<ProductInfo>() {
                    @Override
                    public void onSave(ProductInfo productInfo, Object[] params) {
                        ServiceFacade.getProductService().save(productInfo);

                        if (!(Boolean) params[0])
                            dialogSupport.hidePopupDialog();

                        row.setItem(null);
                    }

                    @Override
                    public void onCancel() {
                        dialogSupport.hidePopupDialog();
                    }
                });
            }

        };

        eppViewComponent = new EppViewComponent(dialogSupport) {
            @Override
            public void onChange(InvoicePdf invoicePdf) {
                invoicePdfViewComponent.updateState();
            }

            @Override
            public void export(String invoiceNumber, List<InvoiceItem> items, String path) {
                ServiceFacade.getInvoiceService().exportToEpp(BigDecimal.valueOf(getCustomerOrder().getMargin()), invoiceNumber, items, path);
            }
        };

        SplitPane top = new SplitPane();
        top.setDividerPosition(1, 0.40);
        top.setOrientation(Orientation.HORIZONTAL);
        top.getItems().addAll(invoicePdfViewComponent, rawInvoiceItemViewComponent);

        invoiceSplitPane.setDividerPosition(1, 0.40);
        invoiceSplitPane.getItems().addAll(top, eppViewComponent);

        setCenter(invoiceSplitPane);
    }

    public void setOrderData(OrderViewPage.OrderData orderData) {
        this.orderData = orderData;
        invoicePdfViewComponent.setItems(orderData.invoicePdfs);
        updateRawInvoiceTableView();

    }

    private void updateRawInvoiceTableView() {
        updateRawInvoiceTableView(invoicePdfViewComponent.getSelected());
    }

    private void updateRawInvoiceTableView(InvoicePdf selected) {
        if (selected != null)
            rawInvoiceItemViewComponent.setItems(selected.getProducts());
        else
            rawInvoiceItemViewComponent.setItems(Collections.<RawInvoiceProductItem>emptyList());

        if (!getCustomerOrder().isSynthetic())
            eppViewComponent.setActive(selected);
    }

    public void refreshInvoicePdfs() {
        invoicePdfViewComponent.setItems(orderData.invoicePdfs);
        updateRawInvoiceTableView();
    }

    public void update(InvoicePdf invoicePdf) {
        invoicePdfViewComponent.update(invoicePdf);
    }

    public void setEppPrefix(String prefix) {
        rawInvoiceItemViewComponent.setEppPrefix(prefix);
    }

    public void updateView() {
        if (getCustomerOrder().isSynthetic()) {
            invoiceSplitPane.getItems().remove(eppViewComponent);
            invoicePdfViewComponent.getSyncBtn().setVisible(false);
        }
    }

    public interface InvoiceComponentListener {
        void onSync(boolean clear);

        void onSave(InvoicePdf invoicePdf);

        void onImport(List<File> files, CallBack<List<RawInvoiceProductItem>> callBack);

        void onUpload(InvoicePdf invoicePdf);

        void onRawItemsUpdate();
    }

    public void setListener(InvoiceComponentListener listener) {
        this.listener = listener;
    }

    protected abstract CustomerOrder getCustomerOrder();
}
