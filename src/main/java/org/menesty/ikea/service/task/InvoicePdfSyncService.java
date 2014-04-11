package org.menesty.ikea.service.task;

import com.google.gson.Gson;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.service.ServiceFacade;

import java.util.ArrayList;
import java.util.List;

public class InvoicePdfSyncService extends BaseInvoiceSyncService {
    private SimpleObjectProperty<InvoicePdf> invoice = new SimpleObjectProperty<>();

    @Override
    protected Task<Boolean> createTask() {
        final InvoicePdf _invoice = invoice.get();
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<WarehouseItemDto> result = new ArrayList<>();

                for (InvoiceItem item : ServiceFacade.getInvoiceItemService().loadBy(_invoice))
                    result.add(convert(0, item));

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

    public InvoicePdf getInvoice() {
        return invoice.get();
    }
}
