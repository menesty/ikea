package org.menesty.ikea.ui.pages.ikea.order.component.stock;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import org.menesty.ikea.beans.property.SimpleBigDecimalProperty;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.ReturnedItem;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.ikea.order.component.service.DeleteStockItemService;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;

/**
 * Created by Menesty on
 * 6/16/16.
 * 10:30.
 */
public class ReturnItemTableView extends BaseTableView<ReturnedItem> {
  public ReturnItemTableView(DeleteStockItemService deleteStockItemService) {
    {
      TableColumn<ReturnedItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));

      column.setMinWidth(120);
      column.setCellValueFactory(ColumnUtil.column("invoiceItem.artNumber"));
      column.setCellFactory(ArtNumberCell::new);

      getColumns().add(column);
    }

    {
      TableColumn<ReturnedItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));

      column.setCellValueFactory(ColumnUtil.number("count"));

      getColumns().add(column);
    }

    {
      TableColumn<ReturnedItem, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.AMOUNT));

      column.setCellValueFactory(param -> {
        if (param.getValue() != null) {
          ReturnedItem returnedItem = param.getValue();
          return new SimpleBigDecimalProperty(returnedItem.getCount().multiply(returnedItem.getInvoiceItem().getPrice()));
        }

        return null;
      });

      getColumns().add(column);
    }

    setRowRenderListener((row, newValue) -> {
      row.setContextMenu(null);

      if (newValue != null) {
        ContextMenu contextMenu = new ContextMenu();

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.DELETE));

          menuItem.setOnAction(event -> {
            deleteStockItemService.setData(DeleteStockItemService.StockAction.DELETE_RETURN_ITEM, newValue.getInvoiceItemId());
            deleteStockItemService.restart();
          });

          contextMenu.getItems().add(menuItem);
        }

        row.setContextMenu(contextMenu);
      }
    });
  }
}
