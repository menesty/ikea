package org.menesty.ikea.ui.pages.ikea.resumption.service;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.ResumptionItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.xls.XlsExportService;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by Menesty on
 * 3/11/16.
 * 10:34.
 */
public class XlsResumptionItemExportService extends AbstractAsyncService<Void> {
  private ObjectProperty<List<ResumptionItem>> resumptionItemListProperty = new SimpleObjectProperty<>();
  private ObjectProperty<File> targetFileProperty = new SimpleObjectProperty<>();

  @Override
  protected Task<Void> createTask() {
    final List<ResumptionItem> _resumptionItems = resumptionItemListProperty.get();
    final File _targetFile = targetFileProperty.get();

    return new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        XlsExportService xlsExportService = new XlsExportService();

        Collections.sort(_resumptionItems, (ResumptionItem ri1, ResumptionItem ri2) -> {
          String invoiceName1 = ri1.getInvoice() != null && ri1.getInvoice().getInvoiceName() != null ? ri1.getInvoice().getInvoiceName() : "";
          String invoiceName2 = ri2.getInvoice() != null && ri2.getInvoice().getInvoiceName() != null ? ri2.getInvoice().getInvoiceName() : "";

          return invoiceName1.compareTo(invoiceName2);
        });

        xlsExportService.exportResumptionItems(_targetFile, _resumptionItems);
        return null;
      }
    };
  }

  public void setData(File targetFile, List<ResumptionItem> resumptionItems) {
    resumptionItemListProperty.setValue(resumptionItems);
    targetFileProperty.setValue(targetFile);
  }
}
