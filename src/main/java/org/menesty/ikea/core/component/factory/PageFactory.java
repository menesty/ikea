package org.menesty.ikea.core.component.factory;

import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.SiteOrderPage;
import org.menesty.ikea.ui.pages.ikea.contraget.ContragentPage;
import org.menesty.ikea.ui.pages.ikea.log.WarehouseScanLogPage;
import org.menesty.ikea.ui.pages.ikea.order.IkeaOrderViewPage;
import org.menesty.ikea.ui.pages.ikea.order.IkeaProcessOrderPage;
import org.menesty.ikea.ui.pages.ikea.product.ProductPage;
import org.menesty.ikea.ui.pages.ikea.reports.order.OrderSummaryReportPage;
import org.menesty.ikea.ui.pages.ikea.resumption.ResumptionDetailPage;
import org.menesty.ikea.ui.pages.ikea.resumption.ResumptionPage;
import org.menesty.ikea.ui.pages.ikea.warehouse.SiteWarehousePage;
import org.menesty.ikea.ui.pages.wizard.order.OrderCreateWizardPage;

/**
 * Created by Menesty on
 * 10/11/14.
 * 15:33.
 */
public class PageFactory {

  public static BasePage createPage(Class<? extends BasePage> page) {
    if (SiteOrderPage.class.equals(page)) {
      return new SiteOrderPage();
    } else if (OrderCreateWizardPage.class.equals(page)) {
      return new OrderCreateWizardPage();
    } else if (IkeaProcessOrderPage.class.equals(page)) {
      return new IkeaProcessOrderPage();
    } else if (IkeaOrderViewPage.class.equals(page)) {
      return new IkeaOrderViewPage();
    } else if (SiteWarehousePage.class.equals(page)) {
      return new SiteWarehousePage();
    } else if (ContragentPage.class.equals(page)) {
      return new ContragentPage();
    } else if (ResumptionPage.class.equals(page)) {
      return new ResumptionPage();
    } else if (ResumptionDetailPage.class.equals(page)) {
      return new ResumptionDetailPage();
    } else if (WarehouseScanLogPage.class.equals(page)) {
      return new WarehouseScanLogPage();
    } else if (ProductPage.class.equals(page)) {
      return new ProductPage();
    } else if (OrderSummaryReportPage.class.equals(page)) {
      return new OrderSummaryReportPage();
    }

    throw new RuntimeException(String.format("Requested page not defined for creation: %s, please add it to Page factory", page.getName()));
  }
}
