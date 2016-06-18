package org.menesty.ikea.service;

import org.menesty.ikea.ApplicationPreference;
import org.menesty.ikea.lib.service.IkeaProductService;
import org.menesty.ikea.lib.service.parse.pdf.invoice.InvoicePdfParserService;
import org.menesty.ikea.service.xls.XlsExportService;
import org.menesty.ikea.ui.pages.ikea.order.export.IkeaExportService;
import org.menesty.ikea.util.ErrorConsole;

public class ServiceFacade {

  private static ApplicationPreference applicationPreference;

  private static ErrorConsole errorConsole;

  private final static IkeaProductService ikeaProductService;

  private final static XlsExportService xlsExportService;

  private final static IkeaExportService ikeaExportService;

  private final static InvoicePdfParserService invoicePdfParserService;

  static {
    applicationPreference = new ApplicationPreference();
    errorConsole = new ErrorConsole();
    ikeaProductService = new IkeaProductService();
    xlsExportService = new XlsExportService();
    ikeaExportService = new IkeaExportService();
    invoicePdfParserService = new InvoicePdfParserService();
  }


  public static ApplicationPreference getApplicationPreference() {
    return applicationPreference;
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

  public static IkeaExportService getIkeaExportService() {
    return ikeaExportService;
  }

  public static InvoicePdfParserService getInvoicePdfParserService() {
    return invoicePdfParserService;
  }
}

