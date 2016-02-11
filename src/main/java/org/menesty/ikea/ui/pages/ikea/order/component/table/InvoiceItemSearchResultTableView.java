package org.menesty.ikea.ui.pages.ikea.order.component.table;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;

/**
 * Created by Menesty on
 * 11/26/15.
 * 13:34.
 */
public class InvoiceItemSearchResultTableView<T> extends TableView<T> {
  public InvoiceItemSearchResultTableView() {
    {
      TableColumn<T, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setPrefWidth(125);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);

      getColumns().add(column);
    }

    {
      TableColumn<T, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SHORT_NAME));
      column.setPrefWidth(170);
      column.setCellValueFactory(ColumnUtil.column("shortName"));

      getColumns().add(column);
    }

    {
      TableColumn<T, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));
      column.setPrefWidth(50);
      column.setCellValueFactory(ColumnUtil.number("count"));

      getColumns().add(column);
    }

    {
      TableColumn<T, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
      column.setPrefWidth(60);
      column.setCellValueFactory(ColumnUtil.number("price"));

      getColumns().add(column);
    }

    {
      TableColumn<T, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.WAT));
      column.setPrefWidth(40);
      column.setCellValueFactory(ColumnUtil.number("wat"));

      getColumns().add(column);
    }
  }
}
