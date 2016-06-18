package org.menesty.ikea.ui.pages.ikea.warehouse.dialog;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.paragon.Paragon;
import org.menesty.ikea.lib.domain.ikea.logistic.paragon.ParagonItem;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;

public class ParagonViewDialog extends BaseDialog {

  private TableView<ParagonItem> tableView;

  public ParagonViewDialog(Stage stage) {
    super(stage);
    setMinWidth(530);

    setTitle(I18n.UA.getString(I18nKeys.PARAGON_DETAILS));

    tableView = new TableView<>();

    {
      TableColumn<ParagonItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setMinWidth(150);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);
      tableView.getColumns().add(column);
    }

    {
      TableColumn<ParagonItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SHORT_NAME));
      column.setMinWidth(210);
      column.setCellValueFactory(ColumnUtil.<ParagonItem, String>column("shortName"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<ParagonItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));
      column.setMaxWidth(50);
      column.setCellValueFactory(ColumnUtil.<ParagonItem>number("count"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<ParagonItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
      column.setMaxWidth(55);
      column.setCellValueFactory(ColumnUtil.number("price"));
      tableView.getColumns().add(column);
    }

    addRow(tableView);

    cancelBtn.setVisible(false);
  }

  public void show(Paragon paragon) {
    tableView.getItems().setAll(paragon.getParagonItems());
  }
}
