package org.menesty.ikea.ui.pages.ikea.order.dialog.invoice;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.service.parse.pdf.invoice.RawInvoiceItem;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;

/**
 * Created by Menesty on
 * 10/6/15.
 * 18:22.
 */
public class RawInvoiceItemTableView extends TableView<RawInvoiceItem> {
    public RawInvoiceItemTableView() {
        {
            TableColumn<RawInvoiceItem, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.INDEX_NUMBER));

            column.setCellValueFactory(ColumnUtil.indexColumn());
            column.setMaxWidth(40);
            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));

            column.setCellValueFactory(ColumnUtil.column("artNumber"));
            column.setMinWidth(140);
            column.setCellFactory(ArtNumberCell::new);
            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.number("count"));
            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
            column.setMinWidth(100);
            column.setCellValueFactory(ColumnUtil.number("price"));
            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.AMOUNT));
            column.setMinWidth(100);
            column.setCellValueFactory(ColumnUtil.number("amount"));
            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.WAT));
            column.setMinWidth(60);
            column.setCellValueFactory(ColumnUtil.number("wat"));
            getColumns().add(column);
        }

    }
}