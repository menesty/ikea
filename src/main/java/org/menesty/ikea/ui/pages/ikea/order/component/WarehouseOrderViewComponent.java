package org.menesty.ikea.ui.pages.ikea.order.component;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.WarehouseAvailableItem;
import org.menesty.ikea.lib.domain.order.IkeaClientOrderItemDto;
import org.menesty.ikea.lib.domain.order.IkeaOrderDetail;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 10/26/15.
 * 06:39.
 */
public class WarehouseOrderViewComponent extends BorderPane {
  private WarehouseAvailableLoadService loadService;
  private Map<Long, Profile> profiles;
  private List<WarehouseAvailableItem> warehouseAvailableItems;
  private TableView<WarehouseAvailableItem> tableView;

  public WarehouseOrderViewComponent() {
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
      TableColumn<WarehouseAvailableItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setMinWidth(140);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<WarehouseAvailableItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));

      column.setCellValueFactory(ColumnUtil.number("available"));
      column.setMinWidth(60);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<WarehouseAvailableItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));

      column.setCellValueFactory(ColumnUtil.number("price"));
      column.setMinWidth(60);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<WarehouseAvailableItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.WEIGHT));

      column.setCellValueFactory(ColumnUtil.number("weight"));
      column.setMaxWidth(60);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<WarehouseAvailableItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.CLIENT));

      column.setCellValueFactory(param -> {
        if (param.getValue() != null && profiles.containsKey(param.getValue().getProfileId())) {
          Profile profile = profiles.get(param.getValue().getProfileId());
          return new SimpleStringProperty(profile.getFirstName() + " " + profile.getLastName());
        }

        return null;
      });
      column.setPrefWidth(100);

      tableView.getColumns().add(column);
    }

    LoadingPane loadingPane = new LoadingPane();

    loadService = new WarehouseAvailableLoadService();
    loadService.setOnSucceededListener(value -> {
      warehouseAvailableItems = value;
      tableView.getItems().setAll(warehouseAvailableItems);
    });
    loadingPane.bindTask(loadService);

    StackPane mainPane = new StackPane();
    mainPane.getChildren().addAll(tableView, loadingPane);

    setCenter(mainPane);
  }

  private void applyFilter(String artNumber) {
    List<WarehouseAvailableItem> items;
    if (StringUtils.isNotBlank(artNumber)) {
      items = warehouseAvailableItems.stream()
          .filter(warehouseAvailableItem -> warehouseAvailableItem.getArtNumber().contains(artNumber))
          .collect(Collectors.toList());
    } else {
      items = warehouseAvailableItems;
    }
    tableView.getItems().setAll(items);

  }

  public void setOrderDetail(IkeaOrderDetail ikeaOrderDetail) {
    this.profiles = ikeaOrderDetail.getIkeaClientOrderItemDtos().stream()
        .map(IkeaClientOrderItemDto::getProfile)
        .distinct()
        .collect(Collectors.toMap(Profile::getId, Function.<Profile>identity()));
    loadService.setIkeaProcessOrderId(ikeaOrderDetail.getId());
    load();
  }

  public void load() {
    loadService.restart();
  }

  class WarehouseAvailableLoadService extends AbstractAsyncService<List<WarehouseAvailableItem>> {
    private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();

    @Override
    protected Task<List<WarehouseAvailableItem>> createTask() {
      final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();
      return new Task<List<WarehouseAvailableItem>>() {
        @Override
        protected List<WarehouseAvailableItem> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/ikea-order/warehouse/" + _ikeaProcessOrderId);

          return request.getList(new TypeReference<List<WarehouseAvailableItem>>() {
          });
        }
      };
    }

    public void setIkeaProcessOrderId(Long ikeaProcessOrderId) {
      ikeaProcessOrderIdProperty.setValue(ikeaProcessOrderId);
    }
  }
}

