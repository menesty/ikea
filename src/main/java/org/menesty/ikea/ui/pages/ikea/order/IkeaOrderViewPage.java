package org.menesty.ikea.ui.pages.ikea.order;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.invoice.Invoice;
import org.menesty.ikea.lib.domain.order.IkeaOrderDetail;
import org.menesty.ikea.lib.domain.order.IkeaProcessOrder;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.ikea.order.component.InvoiceViewComponent;
import org.menesty.ikea.ui.pages.ikea.order.component.RawOrderViewComponent;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.List;

/**
 * Created by Menesty on
 * 9/29/15.
 * 10:22.
 */
public class IkeaOrderViewPage extends BasePage {
    private RawOrderViewComponent rawOrderViewComponent;
    private LoadService loadService;
    private TabPane tabPane;
    private InvoiceViewComponent invoiceViewComponent;
    private IkeaOrderDetail ikeaOrderDetail;

    @Override
    protected void initialize() {
        loadService = new LoadService();
        loadService.setOnSucceededListener(value -> {
            rawOrderViewComponent.setData(value.getIkeaClientOrderItemDtos());
            Tab first = tabPane.getTabs().get(0);
            first.setText(first.getText() + " : " + value.getName());
            this.ikeaOrderDetail = value;

            invoiceViewComponent.setProcessOrderId(ikeaOrderDetail.getId());
            invoiceViewComponent.setInvoices(ikeaOrderDetail.getInvoices());
        });
    }

    @Override
    protected Node createView() {
        tabPane = new TabPane();

        {
            Tab tab = new Tab(I18n.UA.getString(I18nKeys.CLIENTS_ORDER));
            tab.setClosable(false);
            tab.setContent(rawOrderViewComponent = new RawOrderViewComponent(getDialogSupport()));
            tabPane.getTabs().add(tab);
        }

        {
            Tab tab = new Tab(I18n.UA.getString(I18nKeys.INVOICES_NAME));
            tab.setClosable(false);
            tab.setContent(invoiceViewComponent = new InvoiceViewComponent(getDialogSupport(), new InvoiceViewComponent.InvoiceActionListener() {
                @Override
                public void onExport(List<Invoice> invoices) {
                    ikeaOrderDetail.addInvoices(invoices);
                    invoiceViewComponent.setInvoices(ikeaOrderDetail.getInvoices());
                }

                @Override
                public void onInvoiceDelete(Invoice invoice) {
                    ikeaOrderDetail.getInvoices().remove(invoice);
                    invoiceViewComponent.setInvoices(ikeaOrderDetail.getInvoices());
                }

                @Override
                public void onInvoiceAdd(Invoice invoice) {
                    ikeaOrderDetail.getInvoices().add(invoice);
                    invoiceViewComponent.setInvoices(ikeaOrderDetail.getInvoices());
                }
            }));

            tabPane.getTabs().add(tab);
        }

        return wrap(tabPane);
    }

    @Override
    public void onActive(Object... params) {
        loadingPane.bindTask(loadService);
        loadService.setProcessOrderId(this.<IkeaProcessOrder>cast(params[0]).getId());
        loadService.restart();
    }

    class LoadService extends AbstractAsyncService<IkeaOrderDetail> {
        private LongProperty processOrderIdProperty = new SimpleLongProperty();

        @Override
        protected Task<IkeaOrderDetail> createTask() {
            final Long _id = processOrderIdProperty.get();
            return new Task<IkeaOrderDetail>() {
                @Override
                protected IkeaOrderDetail call() throws Exception {
                    APIRequest apiRequest = HttpServiceUtil.get("/ikea-order-detail/" + _id);

                    return apiRequest.getData(IkeaOrderDetail.class);
                }
            };
        }

        public void setProcessOrderId(Long id) {
            processOrderIdProperty.set(id);
        }
    }
}


