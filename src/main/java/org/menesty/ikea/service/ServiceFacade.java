package org.menesty.ikea.service;

public class ServiceFacade {

    private static OrderService orderService;

    private static InvoiceItemService invoiceItemService;

    private static InvoicePdfService invoicePdfService;

    static {
        orderService = new OrderService();
        invoiceItemService = new InvoiceItemService();
        invoicePdfService = new InvoicePdfService();
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
}
