package org.menesty.ikea.ui.pages;

import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;

public enum Pages {
  ORDERS("Orders"), SHOPS("Shops"), PRODUCTS("Products"), USERS("Users"), WAREHOUSE("Warehouse"), INVOICE("Invoice"),
  IKEA_PARAGONS("Ikea paragons"), CUSTOMER_ORDER("Customer Order"), INVOICE_ITEM_SEARCH("Invoice Search"),
  SITE_ORDERS("Site orders"), ORDER_WIZARD("Order Create Wizard"), ORDER_DETAIL("Order Details"),
  CONTRAGENT(I18n.UA.getString(I18nKeys.CONTRAGENTS));

  private final String title;

  Pages(String title) {
    this.title = title;

  }

  public String getTitle() {
    return title;
  }
}
