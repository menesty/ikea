package org.menesty.ikea.service.task;

import javafx.concurrent.Task;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.InvoicePdf;

public class InvoiceSyncTask extends Task<Void> {
    private CustomerOrder customerOrder;

    @Override
    protected Void call() throws Exception {
        for (InvoicePdf invoicePdf : customerOrder.getInvoicePdfs()) {


        }

        return null;
    }
}
