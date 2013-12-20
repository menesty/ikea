package org.menesty.ikea.service;

public class ServiceFacade {

    private static OrderService orderService;


    static {
        orderService = new OrderService();
    }


    public static OrderService getOrderService() {
        return orderService;
    }

}
