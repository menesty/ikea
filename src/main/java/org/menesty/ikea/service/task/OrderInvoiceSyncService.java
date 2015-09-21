package org.menesty.ikea.service.task;

import com.google.gson.Gson;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.util.NumberUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderInvoiceSyncService extends BaseInvoiceSyncService {
    private SimpleObjectProperty<CustomerOrder> customerOrder = new SimpleObjectProperty<>();
    private SimpleBooleanProperty clear = new SimpleBooleanProperty();
    private SimpleObjectProperty<BigDecimal> margin = new SimpleObjectProperty<>();

    @Override
    protected Task<Boolean> createTask() {
        final CustomerOrder _order = customerOrder.get();
        final boolean _clear = clear.get();
        final BigDecimal _margin = margin.get() == null || margin.get().equals(BigDecimal.ZERO) ? null : margin.get();

        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<WarehouseItemDto> result = new ArrayList<>();
                int orderId = ((int) NumberUtil.parse(_order.getName()));

                result.addAll(ServiceFacade.getInvoiceItemService().loadBy(_order)
                        .stream()
                        .map(item -> convert(_margin, orderId, item))
                        .collect(Collectors.toList()));

                try {
                    sendData(_clear, new Gson().toJson(result));
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

    public void setClear(boolean clear) {
        this.clear.set(clear);
    }

    public void setMargin(BigDecimal margin) {
        this.margin.set(margin);
    }
}
