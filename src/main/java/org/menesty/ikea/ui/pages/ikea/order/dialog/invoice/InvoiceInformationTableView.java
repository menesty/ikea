package org.menesty.ikea.ui.pages.ikea.order.dialog.invoice;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.service.parse.pdf.invoice.InvoiceParseResult;
import org.menesty.ikea.lib.service.parse.pdf.invoice.RawInvoiceItem;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.NumberUtil;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 10/5/15.
 * 22:16.
 */
class InvoiceInformationTableView extends TableView<InvoiceParseResult> {
  public InvoiceInformationTableView() {
    {
      TableColumn<InvoiceParseResult, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.FILE_NAME));
      column.setPrefWidth(100);
      column.setCellValueFactory(ColumnUtil.column("invoiceInformation.fileName"));

      getColumns().add(column);
    }

    {
      TableColumn<InvoiceParseResult, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.INVOICE_NAME));
      column.setPrefWidth(120);
      column.setCellValueFactory(ColumnUtil.column("invoiceInformation.invoiceName"));

      getColumns().add(column);
    }

    {
      TableColumn<InvoiceParseResult, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PARAGON_NUMBER));
      column.setCellValueFactory(ColumnUtil.column("invoiceInformation.paragonNumber"));
      column.setPrefWidth(90);
      getColumns().add(column);
    }

    {
      TableColumn<InvoiceParseResult, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SELL_DATE));
      column.setCellValueFactory(ColumnUtil.dateColumn("invoiceInformation.sellDate"));
      column.setPrefWidth(110);
      getColumns().add(column);
    }

    {
      TableColumn<InvoiceParseResult, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.AMOUNT));
      column.setCellValueFactory(ColumnUtil.number("invoiceInformation.payed"));
      column.setPrefWidth(80);
      getColumns().add(column);
    }

    {
      TableColumn<InvoiceParseResult, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ITEM_AMOUNT));
      column.setCellValueFactory(param -> {
        if (param.getValue() != null) {
          BigDecimal itemsAmount = param.getValue().getRawInvoiceItems().stream().map(RawInvoiceItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

          boolean same = itemsAmount.compareTo(param.getValue().getInvoiceInformation().getPayed()) == 0;

          return new SimpleStringProperty(NumberUtil.toString(itemsAmount.doubleValue()) + (same ? "" : " "));
        }

        return new SimpleStringProperty("");
      });

      column.setCellFactory(param -> new TableCell<InvoiceParseResult, String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
          super.updateItem(item, empty);
          if (!empty) {
            this.setText(item);
            setTextFill((!isEmpty() && item.endsWith(" ")) ? Color.RED : Color.BLACK);
          }
        }
      });

      column.setPrefWidth(120);
      getColumns().add(column);
    }
  }
}
