package org.menesty.ikea.ui.pages;

public enum Pages {
    ORDERS("Orders"), SHOPS("Shops"), PRODUCTS("Products"), USERS("Users"), WAREHOUSE("Warehouse"), INVOICE("Invoice"),
    IKEA_PARAGONS("Ikea paragons"), CUSTOMER_ORDER("Customer Order"), INVOICE_ITEM_SEARCH("Invoice Search");

    private final String title;

    Pages(String title) {
        this.title = title;

    }

    public String getTitle() {
        return title;
    }
}
