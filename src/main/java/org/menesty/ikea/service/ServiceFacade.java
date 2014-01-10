package org.menesty.ikea.service;

import org.menesty.ikea.ApplicationPreference;

public class ServiceFacade {

    private static OrderService orderService;

    private static InvoiceItemService invoiceItemService;

    private static InvoicePdfService invoicePdfService;

    private static ApplicationPreference applicationPreference;

    private static ProductService productService;

    private static OrderItemService orderItemService;

    static {
        orderService = new OrderService();
        invoiceItemService = new InvoiceItemService();
        invoicePdfService = new InvoicePdfService();
        applicationPreference = new ApplicationPreference();
        productService = new ProductService();
        orderItemService = new OrderItemService();
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

    public static ProductService getProductService() {
        return productService;
    }

    public static OrderItemService getOrderItemService() {
        return orderItemService;
    }
}
