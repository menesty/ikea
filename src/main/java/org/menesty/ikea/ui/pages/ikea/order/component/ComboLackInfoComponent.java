package org.menesty.ikea.ui.pages.ikea.order.component;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.NotCompleteCombo;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.NotCompleteComboPart;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;

import java.util.List;

/**
 * Created by Menesty on
 * 6/1/16.
 * 21:32.
 */
public class ComboLackInfoComponent extends BorderPane {
  private TableView<NotCompleteComboPart> partTableView;
  private TableView<NotCompleteCombo> comboTableView;

  public ComboLackInfoComponent() {
    partTableView = new TableView<>();

    {
      TableColumn<NotCompleteComboPart, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setMinWidth(110);
      column.setCellFactory(ArtNumberCell::new);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));

      partTableView.getColumns().add(column);
    }

    {
      TableColumn<NotCompleteComboPart, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.EXPECTED_COUNT));
      column.setMinWidth(120);
      column.setCellValueFactory(ColumnUtil.number("expected"));

      partTableView.getColumns().add(column);
    }

    {
      TableColumn<NotCompleteComboPart, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.BOUGHT_COUNT));
      column.setMinWidth(120);
      column.setCellValueFactory(ColumnUtil.number("bought"));

      partTableView.getColumns().add(column);
    }

    comboTableView = new TableView<>();

    {
      TableColumn<NotCompleteCombo, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setMinWidth(130);
      column.setCellFactory(ArtNumberCell::new);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));

      comboTableView.getColumns().add(column);
    }

    comboTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        partTableView.getItems().setAll(newValue.getNotCompleteComboParts());
      } else {
        partTableView.getItems().clear();
      }
    });
    comboTableView.setMaxWidth(150);

    setLeft(comboTableView);
    setCenter(partTableView);
  }

  public void setCombos(List<NotCompleteCombo> notCompleteCombos) {
    comboTableView.getItems().setAll(notCompleteCombos);
  }
}
