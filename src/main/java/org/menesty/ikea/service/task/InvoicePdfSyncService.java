package org.menesty.ikea.service.task;

import com.google.gson.Gson;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.service.ServiceFacade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InvoicePdfSyncService extends BaseInvoiceSyncService {
    private SimpleObjectProperty<InvoicePdf> invoice = new SimpleObjectProperty<>();
    private SimpleObjectProperty<BigDecimal> margin = new SimpleObjectProperty<>();

    @Override
    protected Task<Boolean> createTask() {
        final InvoicePdf _invoice = invoice.get();
        final BigDecimal _margin = margin.get() == null || margin.get().equals(BigDecimal.ZERO) ? DEFAULT_MARGIN : margin.get();
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<WarehouseItemDto> result = ServiceFacade.getInvoiceItemService().loadBy(_invoice)
                        .stream()
                        .map(item -> convert(_margin, 0, item))
                        .collect(Collectors.toList());

                try {
                    sendData(false, new Gson().toJson(result));
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }
        };
    }

    public void setInvoice(InvoicePdf invoice) {
        this.invoice.set(invoice);
    }

    public void setMargin(BigDecimal margin) {
        this.margin.set(margin);
    }

    public InvoicePdf getInvoice() {
        return invoice.get();
    }
}
