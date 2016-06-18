package org.menesty.ikea.ui.pages;

import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;

public enum Pages {
  ORDERS("Orders"), SHOPS("Shops"), PRODUCTS("Products"), USERS("Users"), WAREHOUSE("Warehouse"), INVOICE("Invoice"),
  IKEA_PARAGONS("Ikea paragons"), CUSTOMER_ORDER("Customer Order"), INVOICE_ITEM_SEARCH("Invoice Search"),
  SITE_ORDERS("Site orders"), ORDER_WIZARD("Order Create Wizard"), ORDER_DETAIL("Order Details"), SCAN_LOG("Scan log"),
  CONTRAGENT(I18n.UA.getString(I18nKeys.CONTRAGENTS)), RESUMPTION(I18n.UA.getString(I18nKeys.RESUMPTION)),
  ORDER_REPORT(I18n.UA.getString(I18nKeys.ORDER_REPORT)),
  RESUMPTION_DETAIL(I18n.UA.getString(I18nKeys.RESUMPTION_DETAIL));


  private final String title;

  Pages(String title) {
    this.title = title;

  }

  public String getTitle() {
    return title;
  }
}
