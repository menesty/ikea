package org.menesty.ikea.ui.pages.ikea.warehouse;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.WarehouseAvailableItem;
import org.menesty.ikea.lib.domain.ikea.logistic.warehouse.WarehouseStatusDto;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.ParagonManageDialog;
import org.menesty.ikea.ui.controls.search.WarehouseItemSearchData;
import org.menesty.ikea.ui.controls.search.WarehouseSearchBar;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WarehouseViewComponent extends BorderPane {
  private final LoadService loadService;
  private TableView<WarehouseAvailableItem> tableView;

  private WarehouseStatusDto warehouseStatusDto;
  private WarehouseSearchBar warehouseSearchBar;

  private ParagonManageDialog paragonManageDialog;

  private StatusPanel statusPanel;

  public WarehouseViewComponent(final DialogSupport dialogSupport) {
    paragonManageDialog = new ParagonManageDialog(dialogSupport.getStage()) {
      @Override
      public void onCreate() {
        load();
      }
    };

    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> {
      warehouseStatusDto = value;
      setItems(value.getItems());
      warehouseSearchBar.setProfiles(warehouseStatusDto.getProfiles());
    });

    tableView = new TableView<>();

    {
      TableColumn<WarehouseAvailableItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setMinWidth(150);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<WarehouseAvailableItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ORDER_NAME));
      column.setMinWidth(140);
      column.setCellValueFactory(ColumnUtil.column("orderName"));

      tableView.getColumns().add(column);

    }

    {
      TableColumn<WarehouseAvailableItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));

      column.setCellValueFactory(ColumnUtil.number("available"));
      column.setMinWidth(60);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<WarehouseAvailableItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.WEIGHT));

      column.setCellValueFactory(ColumnUtil.grToKg("weight"));
      column.setMaxWidth(60);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<WarehouseAvailableItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.CLIENT));

      column.setCellValueFactory(param -> {
        if (param == null) {
          return new SimpleStringProperty();
        }

        return new SimpleStringProperty(warehouseSearchBar.getProfileById(param.getValue().getProfileId()));
      });
      column.setMinWidth(250);

      tableView.getColumns().add(column);
    }


    VBox controlBox = new VBox();

    {
      ToolBar control = new ToolBar();

      {
        Button button = new Button(null, ImageFactory.createReload32Icon());
        button.setOnAction(actionEvent -> load());

        control.getItems().add(button);
      }

      /*{
        Button button = new Button(null, ImageFactory.createAdd32Icon());
        button.setOnAction(actionEvent -> {
          paragonManageDialog.show(getChecked());
          dialogSupport.showPopupDialog(paragonManageDialog);
        });
        control.getItems().add(button);
      }*/

      controlBox.getChildren().add(control);
    }
    controlBox.getChildren().add(warehouseSearchBar = new WarehouseSearchBar() {
      @Override
      public void onSearch(WarehouseItemSearchData data) {
        Stream<WarehouseAvailableItem> stream = warehouseStatusDto.getItems().parallelStream();

        if (data.getProfileId() != null) {
          stream = stream.filter(warehouseAvailableItem -> warehouseAvailableItem.getProfileId().equals(data.getProfileId()));
        }

        if (data.getArtNumber() != null) {
          stream = stream.filter(warehouseAvailableItem -> warehouseAvailableItem.getArtNumber().contains(data.getArtNumber()));
        }

        setItems(stream.collect(Collectors.toList()));
      }
    });

    setCenter(tableView);
    setTop(controlBox);
    setBottom(statusPanel = new StatusPanel());

  }

  private void setItems(List<WarehouseAvailableItem> items) {
    tableView.getItems().setAll(items);

    BigDecimal total = items.stream()
        .map(WarehouseAvailableItem::getTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    statusPanel.setTotal(total);
  }


  /*private List<WarehouseItemDto> getChecked() {
    List<WarehouseItemDto> result = new ArrayList<>();

    for (WarehouseItemDtoTableItem item : tableView.getItems())
      if (item.isChecked())
        result.add(item.getItem());

    return result;
  }*/

  private List<WarehouseItemDtoTableItem> transform(List<WarehouseItemDto> items) {
    List<WarehouseItemDtoTableItem> result = new ArrayList<>();

    for (WarehouseItemDto item : items)
      result.add(new WarehouseItemDtoTableItem(false, item));

    return result;
  }


  public void load() {
    loadService.restart();
  }

  public Collection<? extends Worker<?>> getWorkers() {
    return Collections.singletonList(loadService);
  }

  public class WarehouseItemDtoTableItem {

    private BooleanProperty checked;

    private final WarehouseItemDto item;

    public WarehouseItemDtoTableItem(boolean checked, WarehouseItemDto item) {
      this.item = item;
      this.checked = new SimpleBooleanProperty(checked);

      this.checked.addListener((observableValue, oldValue, newValue) -> {
        WarehouseItemDtoTableItem.this.checked.getValue();

      });
      this.checked.addListener(observable -> {
        WarehouseItemDtoTableItem.this.checked.getValue();
      });
    }

    public BooleanProperty checkedProperty() {
      return checked;
    }

    public boolean isChecked() {
      return checked.get();
    }

    public WarehouseItemDto getItem() {
      return item;
    }

  }


  class LoadService extends AbstractAsyncService<WarehouseStatusDto> {
    @Override
    protected Task<WarehouseStatusDto> createTask() {
      return new Task<WarehouseStatusDto>() {
        @Override
        protected WarehouseStatusDto call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/warehouse/status");

          return request.getData(WarehouseStatusDto.class);
        }
      };
    }
  }

  class StatusPanel extends TotalStatusPanel {

  }
}


