package org.menesty.ikea.ui.pages.ikea.order.component.table;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StockItemDto;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;

/**
 * Created by Menesty on
 * 11/27/15.
 * 14:34.
 */
public class StockItemTableView extends TableView<StockItemDto> {
  public StockItemTableView() {
    {
      TableColumn<StockItemDto, Number> column = new TableColumn<>();
      column.setMaxWidth(40);
      column.setCellValueFactory(ColumnUtil.<StockItemDto>indexColumn());

      getColumns().add(column);
    }

    {
      TableColumn<StockItemDto, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));

      column.setMinWidth(140);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);
      getColumns().add(column);
    }

    {
      TableColumn<StockItemDto, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));

      column.setMinWidth(60);
      column.setCellValueFactory(ColumnUtil.number("count"));

      getColumns().add(column);
    }

    {
      TableColumn<StockItemDto, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));

      column.setMinWidth(80);
      column.setCellValueFactory(ColumnUtil.number("price"));

      getColumns().add(column);
    }

    {
      TableColumn<StockItemDto, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.CLIENT));

      column.setMinWidth(120);
      column.setCellValueFactory(ColumnUtil.column("profileInfo"));

      getColumns().add(column);
    }
  }
}
