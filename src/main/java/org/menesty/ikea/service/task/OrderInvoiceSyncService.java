package org.menesty.ikea.service.task;

import com.google.gson.Gson;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.util.NumberUtil;

import java.util.ArrayList;
import java.util.List;

public class OrderInvoiceSyncService extends BaseInvoiceSyncService {
    private SimpleObjectProperty<CustomerOrder> customerOrder = new SimpleObjectProperty<>();

    @Override
    protected Task<Boolean> createTask() {
        final CustomerOrder _order = customerOrder.get();
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<WarehouseItemDto> result = new ArrayList<>();
                //TODO FIX me change to one query
                int orderId = ((int) NumberUtil.parse(_order.getName()));

                for (InvoiceItem item : ServiceFacade.getInvoiceItemService().loadBy(_order))
                    result.add(convert(orderId, item));

                try {
                    sendData(new Gson().toJson(result));
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }
        };
    }

    public void setCustomerOrder(CustomerOrder customerOrder) {
        this.customerOrder.setValue(customerOrder);
    }

}