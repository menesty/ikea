package org.menesty.ikea.ui.pages.ikea.resumption.component;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.ResumptionItem;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.ikea.resumption.dialog.ResumptionItemAlternativeDialog;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;

/**
 * Created by Menesty on
 * 3/9/16.
 * 23:09.
 */
public class ResumptionItemTableView extends BaseTableView<ResumptionItem> {

  private ResumptionItemAlternativeDialog resumptionItemAlternativeDialog;

  public ResumptionItemTableView() {
    this(null);
  }

  public ResumptionItemTableView(DialogSupport dialogSupport) {

    {
      TableColumn<ResumptionItem, Number> column = new TableColumn<>();
      column.setMaxWidth(45);
      column.setCellValueFactory(ColumnUtil.<ResumptionItem>indexColumn());
      getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setMinWidth(130);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);

      getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));

      column.setCellValueFactory(ColumnUtil.number("invoiceItem.price"));
      column.setMinWidth(45);

      getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));

      column.setCellValueFactory(ColumnUtil.number("count"));
      column.setMinWidth(45);

      getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.INVOICE_NAME));
      column.setPrefWidth(140);
      column.setCellValueFactory(ColumnUtil.column("invoice.invoiceName"));

      getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PARAGON_NUMBER));
      column.setCellValueFactory(ColumnUtil.column("invoice.paragonNumber"));
      column.setPrefWidth(100);
      getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SELL_DATE));
      column.setCellValueFactory(ColumnUtil.dateColumn("invoice.sellDate"));
      column.setPrefWidth(140);
      getColumns().add(column);
    }

    if (dialogSupport != null) {
      setRowRenderListener((row, newValue) -> {
        row.setContextMenu(null);
        if (newValue != null) {
          ContextMenu contextMenu = new ContextMenu();

          {
            MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.RESUMPTION_ALTERNATIVES_MENU));
            menuItem.setOnAction(event -> {
              ResumptionItemAlternativeDialog dialog = getResumptionItemAlternativeDialog(dialogSupport);
              dialog.setProductId(newValue.getProductId());

              dialogSupport.showPopupDialog(dialog);
            });

            contextMenu.getItems().add(menuItem);
          }

          row.setContextMenu(contextMenu);
        }
      });
    }
  }

  public ResumptionItemAlternativeDialog getResumptionItemAlternativeDialog(DialogSupport dialogSupport) {
    if (resumptionItemAlternativeDialog == null) {
      resumptionItemAlternativeDialog = new ResumptionItemAlternativeDialog(dialogSupport.getStage()) {
        @Override
        public void onCancel() {
          dialogSupport.hidePopupDialog();
        }

        @Override
        public void onOk() {
          dialogSupport.hidePopupDialog();
        }
      };
    }
    return resumptionItemAlternativeDialog;
  }
}
