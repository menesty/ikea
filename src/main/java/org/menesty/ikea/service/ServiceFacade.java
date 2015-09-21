package org.menesty.ikea.service;

import org.menesty.ikea.ApplicationPreference;
import org.menesty.ikea.service.xls.XlsExportService;
import org.menesty.ikea.ui.pages.wizard.order.step.service.IkeaProductService;
import org.menesty.ikea.util.ErrorConsole;

import java.util.List;

public class ServiceFacade {

    private static OrderService orderService;

    private static InvoiceItemService invoiceItemService;

    private static InvoicePdfService invoicePdfService;

    private static ApplicationPreference applicationPreference;

    private static ProductService productService;

    private static OrderItemService orderItemService;

    private static InvoiceService invoiceService;

    private static IkeaParagonService ikeaParagonService;

    private static IkeaShopService ikeaShopService;

    private static UserService userService;

    private static NewOrderFillService newOrderFillService;

    private static OrderPdfService orderPdfService;

    private static ErrorConsole errorConsole;

    private final static IkeaProductService ikeaProductService;

    private final static XlsExportService xlsExportService;

    static {
        orderService = new OrderService();
        invoiceItemService = new InvoiceItemService();
        invoicePdfService = new InvoicePdfService();
        applicationPreference = new ApplicationPreference();
        productService = new ProductService();
        orderItemService = new OrderItemService();
        invoiceService = new InvoiceService();
        ikeaUserService = new IkeaUserService();
        ikeaParagonService = new IkeaParagonService();
        ikeaShopService = new IkeaShopService();
        userService = new UserService();
        newOrderFillService = new NewOrderFillService();
        orderPdfService = new OrderPdfService();
        errorConsole = new ErrorConsole();
        ikeaProductService = new IkeaProductService();
        xlsExportService = new XlsExportService();
    }

    private static IkeaUserService ikeaUserService;

    public static NewOrderFillService getNewOrderFillService() {
        return newOrderFillService;
    }

    public static OrderService getOrderService() {
        return orderService;
    }

    public static UserService getUserService() {
        return userService;
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

    public static InvoiceService getInvoiceService() {
        return invoiceService;
    }

    public static IkeaUserService getIkeaUserService() {
        return ikeaUserService;
    }

    public static IkeaParagonService getIkeaParagonService() {
        return ikeaParagonService;
    }

    public static IkeaShopService getIkeaShopService() {
        return ikeaShopService;
    }

    public static OrderPdfService getOrderPdfService() {
        return orderPdfService;
    }

    public static ErrorConsole getErrorConsole() {
        return errorConsole;
    }

    public static IkeaProductService getIkeaProductService() {
        return ikeaProductService;
    }

    public static XlsExportService getXlsExportService() {
        return xlsExportService;
    }
}

