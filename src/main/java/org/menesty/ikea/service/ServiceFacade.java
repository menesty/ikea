package org.menesty.ikea.service;

import org.menesty.ikea.ApplicationPreference;

public class ServiceFacade {

    private static OrderService orderService;

    private static InvoiceItemService invoiceItemService;

    private static InvoicePdfService invoicePdfService;

    private static ApplicationPreference applicationPreference;

    static {
        orderService = new OrderService();
        invoiceItemService = new InvoiceItemService();
        invoicePdfService = new InvoicePdfService();
        applicationPreference = new ApplicationPreference();
    }


    public static OrderService getOrderService() {
        return orderService;
    }

    public static InvoiceItemService getInvoiceItemService() {
        return invoiceItemService;
    }

    public static InvoicePdfService getInvoicePdfService() {
        return invoicePdfService;
    }

    public static ApplicationPreference getApplicationPreference() {
        return applicationPreference;
    }
}
