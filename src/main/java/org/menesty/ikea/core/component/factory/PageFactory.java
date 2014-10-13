package org.menesty.ikea.core.component.factory;

import org.menesty.ikea.ui.pages.*;

/**
 * Created by Menesty on
 * 10/11/14.
 * 15:33.
 */
public class PageFactory {

    public static BasePage createPage(Class<? extends BasePage> page) {
        if (OrderListPage.class.equals(page))
            return new OrderListPage();
        else if (OrderViewPage.class.equals(page))
            return new OrderViewPage();
        else if (ProductPage.class.equals(page))
            return new ProductPage();
        else if (UserPage.class.equals(page))
            return new UserPage();
        else if (WarehousePage.class.equals(page))
            return new WarehousePage();
        else if (CustomInvoicePage.class.equals(page))
            return new CustomInvoicePage();
        else if (IkeaParagonPage.class.equals(page))
            return new IkeaParagonPage();
        else if (IkeaShopPage.class.equals(page))
            return new IkeaShopPage();

        throw new RuntimeException(String.format("Requested page not defined for creation: %s", page.getName()));
    }
}