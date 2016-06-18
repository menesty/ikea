package org.menesty.ikea.ui.pages.ikea.order.component;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.paragon.ParagonItem;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.WarehouseAvailableItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 2/26/16.
 * 13:38.
 */
public class ParagonItemSearchComponent extends BorderPane {

  private final TableView<ParagonItem> tableView;
  private final LoadService loadService;
  private List<ParagonItem> paragonItems;

  public ParagonItemSearchComponent(DialogSupport dialogSupport) {
    ToolBar toolBar = new ToolBar();

    {
      TextField artNumber = new TextField();
      artNumber.setDelay(1);
      artNumber.setOnDelayAction(actionEvent -> applyFilter(artNumber.getText()));
      artNumber.setPromptText("Product ID #");

      toolBar.getItems().add(artNumber);
    }

    setTop(toolBar);

    tableView = new TableView<>();

    {
      TableColumn<ParagonItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setMinWidth(150);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ParagonItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));

      column.setCellValueFactory(ColumnUtil.number("count"));
      column.setMinWidth(60);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ParagonItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));

      column.setCellValueFactory(ColumnUtil.number("price"));
      column.setMinWidth(60);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ParagonItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.WEIGHT));

      column.setCellValueFactory(ColumnUtil.number("weight"));
      column.setMaxWidth(60);

      tableView.getColumns().add(column);
    }


    LoadingPane loadingPane = new LoadingPane();

    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> {
      paragonItems = value;
      tableView.getItems().setAll(paragonItems);
    });
    loadingPane.bindTask(loadService);

    StackPane mainPane = new StackPane();
    mainPane.getChildren().addAll(tableView, loadingPane);

    setCenter(mainPane);
  }

  private void applyFilter(String artNumber) {
    List<ParagonItem> items;
    if (StringUtils.isNotBlank(artNumber)) {
      items = paragonItems.stream()
          .filter(warehouseAvailableItem -> warehouseAvailableItem.getArtNumber().contains(artNumber))
          .collect(Collectors.toList());
    } else {
      items = paragonItems;
    }
    tableView.getItems().setAll(items);
  }

  public void onActive(Long ikeaProcessOrderId) {
    loadService.setIkeaProcessOrderId(ikeaProcessOrderId);
    loadService.restart();
  }

  class LoadService extends AbstractAsyncService<List<ParagonItem>> {
    private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();

    @Override
    protected Task<List<ParagonItem>> createTask() {
      final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();

      return new Task<List<ParagonItem>>() {
        @Override
        protected List<ParagonItem> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/ikea-order/" + _ikeaProcessOrderId + "/paragon/items");
          return request.getList(new TypeReference<List<ParagonItem>>() {
          });
        }
      };
    }

    public void setIkeaProcessOrderId(Long ikeaProcessOrderId) {
      ikeaProcessOrderIdProperty.setValue(ikeaProcessOrderId);
    }
  }
}
