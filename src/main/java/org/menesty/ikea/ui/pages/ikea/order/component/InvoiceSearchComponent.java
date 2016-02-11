package org.menesty.ikea.ui.pages.ikea.order.component;

import javafx.scene.control.TableColumn;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.Invoice;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.InvoiceItem;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.InvoiceSearchItem;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.pages.ikea.order.component.table.InvoiceItemSearchResultTableView;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 11/26/15.
 * 13:23.
 */
public class InvoiceSearchComponent extends BorderPane {
  private List<Invoice> invoices;
  private InvoiceItemSearchResultTableView<InvoiceSearchItem> invoiceItemSearchResultTableView;

  public InvoiceSearchComponent() {
    ToolBar toolBar = new ToolBar();

    {
      TextField artNumber = new TextField();
      artNumber.setDelay(1);
      artNumber.setOnDelayAction(actionEvent -> applyFilter(artNumber.getText()));
      artNumber.setPromptText("Product ID #");

      toolBar.getItems().add(artNumber);
    }

    setTop(toolBar);

    invoiceItemSearchResultTableView = new InvoiceItemSearchResultTableView<>();

    {
      TableColumn<InvoiceSearchItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.INVOICE_NAME));
      column.setPrefWidth(150);
      column.setCellValueFactory(ColumnUtil.column("invoiceName"));

      invoiceItemSearchResultTableView.getColumns().add(column);
    }

    {
      TableColumn<InvoiceSearchItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PARAGON_NUMBER));
      column.setPrefWidth(150);
      column.setCellValueFactory(ColumnUtil.column("paragonNumber"));

      invoiceItemSearchResultTableView.getColumns().add(column);
    }

    setCenter(invoiceItemSearchResultTableView);
  }

  public void setInvoices(List<Invoice> invoices) {
    this.invoices = invoices;

    applyFilter(null);
  }

  public void applyFilter(String artNumber) {
    List<InvoiceSearchItem> invoiceSearchItems = invoices.stream()
        .map(invoice -> {
          List<InvoiceItem> items = StringUtils.isNotBlank(artNumber) ?
              invoice.getInvoiceItems().stream()
                  .filter(invoiceItem -> invoiceItem.getArtNumber().contains(artNumber))
                  .collect(Collectors.toList()) : invoice.getInvoiceItems();

          if (items == null) {
            return Collections.<InvoiceSearchItem>emptyList();
          }

          return items.stream().map(invoiceItem -> {
            InvoiceSearchItem item = new InvoiceSearchItem();

            item.setArtNumber(invoiceItem.getArtNumber());
            item.setPrice(invoiceItem.getPrice());
            item.setCount(invoiceItem.getCount());
            item.setInvoiceName(invoice.getInvoiceName());
            item.setParagonNumber(invoice.getParagonNumber());
            item.setShortName(invoiceItem.getShortName());
            item.setWat(invoiceItem.getWat());

            return item;
          }).collect(Collectors.toList());

        }).flatMap(Collection::stream).collect(Collectors.toList());

    invoiceItemSearchResultTableView.getItems().clear();
    invoiceItemSearchResultTableView.getItems().addAll(invoiceSearchItems);
  }
}
