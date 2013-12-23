package org.menesty.ikea.service;

public class ServiceFacade {

    private static OrderService orderService;

    private static InvoiceItemService invoiceItemService;

    static {
        orderService = new OrderService();
        invoiceItemService = new InvoiceItemService();
    }

    public static OrderService getOrderService() {
        return orderService;
    }

    public static InvoiceItemService getInvoiceItemService() {
        return invoiceItemService;
    }
}
