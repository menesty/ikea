package org.menesty.ikea.ui.pages.ikea.order.component;

import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StockItemDto;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StorageCalculationResultDto;
import org.menesty.ikea.lib.domain.order.IkeaClientOrderItemDto;
import org.menesty.ikea.lib.domain.order.IkeaOrderDetail;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.lib.dto.ikea.order.NewOrderItemInfo;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.table.EntityCheckBoxHolder;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.controls.table.component.EntityCheckBoxTableColumn;
import org.menesty.ikea.ui.pages.DialogCallback;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.ikea.order.component.service.AddComboPartService;
import org.menesty.ikea.ui.pages.ikea.order.component.table.StockItemTableView;
import org.menesty.ikea.ui.pages.ikea.order.dialog.ChoiceCountDialog;
import org.menesty.ikea.ui.pages.ikea.order.dialog.combo.OrderViewComboDialog;
import org.menesty.ikea.ui.pages.ikea.order.dialog.export.IkeaSiteExportDialog;
import org.menesty.ikea.ui.pages.wizard.order.step.service.AbstractExportAsyncService;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by Menesty on
 * 10/10/15.
 * 10:29.
 */
public class StockManagementComponent extends BorderPane {
  private AbstractExportAsyncService<XlsExportInfo> xlsResultExportAsyncService;
  private ListView<Profile> profileListView;
  private TableView<StockItemDto> readyStockItemTableView;
  private BaseTableView<EntityCheckBoxHolder<StockItemDto>> overBoughTableView;
  private TableView<StockItemDto> storageLackTableView;

  private StorageCalculationService storageCalculationService;
  private AddToOrderItemService addToOrderItemService;
  private AddComboPartService addComboPartService;
  private IkeaProcessOrderResetStateService ikeaProcessOrderResetStateService;

  private Long ikeaProcessOrderId;
  private OrderViewComboDialog orderViewComboDialog;

  private TotalStatusPanel storageLackTotalStatusPanel;
  private TotalStatusPanel overBoughTotalStatusPanel;

  public StockManagementComponent(DialogSupport dialogSupport) {
    StackPane mainPane = new StackPane();

    HBox groupPane = new HBox();
    groupPane.getChildren().addAll(initLeftPane(), initRightPane(dialogSupport));

    LoadingPane loadingPane = new LoadingPane();

    mainPane.getChildren().addAll(groupPane, loadingPane);

    storageCalculationService = new StorageCalculationService();
    storageCalculationService.setOnSucceededListener(value -> {
      readyStockItemTableView.getItems().setAll(value.getReadyStockItems());
      overBoughTableView.getItems().setAll(
          value.getOverBoughtStockItems().stream()
              .map(stockItemDto -> new EntityCheckBoxHolder<>(false, stockItemDto))
              .collect(Collectors.toList())
      );
      storageLackTableView.getItems().setAll(value.getLackStockItems());

      BigDecimal overBoughTotal = value.getOverBoughtStockItems().stream()
          .map(StockItemDto::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal storageLackTotal = value.getLackStockItems().stream()
          .map(StockItemDto::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      storageLackTotalStatusPanel.setTotal(storageLackTotal);
      overBoughTotalStatusPanel.setTotal(overBoughTotal);
    });

    addToOrderItemService = new AddToOrderItemService();
    addToOrderItemService.setOnSucceededListener(value -> {
      storageCalculationService.setUploading(false);
      storageCalculationService.restart();
    });

    addComboPartService = new AddComboPartService();
    addComboPartService.setOnSucceededListener(value -> {
      storageCalculationService.setUploading(false);
      storageCalculationService.restart();
    });

    ikeaProcessOrderResetStateService = new IkeaProcessOrderResetStateService();
    ikeaProcessOrderResetStateService.setOnSucceededListener(value -> {
      storageCalculationService.setUploading(false);
      storageCalculationService.restart();
    });

    xlsResultExportAsyncService = new AbstractExportAsyncService<XlsExportInfo>() {
      @Override
      protected void export(File file, XlsExportInfo param) {
        ServiceFacade.getXlsExportService().exportBuyResult(file, param.getOverBough(), param.getLack());
      }
    };

    loadingPane.bindTask(storageCalculationService, addToOrderItemService, addComboPartService, ikeaProcessOrderResetStateService);
    setCenter(mainPane);

    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createClear32Icon());
      button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.CLEAR_WAREHOUSE)));
      button.setOnAction(event -> Dialog.confirm(dialogSupport, I18n.UA.getString(I18nKeys.WARNING), I18n.UA.getString(I18nKeys.CONFIRM_CLEAR_WAREHOUSE_QUESTION), new DialogCallback() {
        @Override
        public void onCancel() {

        }

        @Override
        public void onYes() {
          ikeaProcessOrderResetStateService.setIkeaProcessOrderId(ikeaProcessOrderId);
          ikeaProcessOrderResetStateService.restart();
        }
      }));

      toolBar.getItems().add(button);
    }
    {
      Button button = new Button(null, ImageFactory.createXlsExport32Icon());
      button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.XLS_EXPORT)));
      button.setOnAction(event -> {

        File selectedFile = FileChooserUtil.getXls().showSaveDialog(dialogSupport.getStage());

        if (selectedFile != null) {
          xlsResultExportAsyncService.setFile(selectedFile);
          xlsResultExportAsyncService.setParam(new XlsExportInfo(convert(overBoughTableView.getItems()), storageLackTableView.getItems()));
          xlsResultExportAsyncService.restart();
        }
      });

      toolBar.getItems().add(button);
    }

    setTop(toolBar);

  }

  private List<StockItemDto> convert(List<EntityCheckBoxHolder<StockItemDto>> items) {
    return items.stream().map(EntityCheckBoxHolder::getItem).collect(Collectors.toList());
  }

  public void setOrderDetail(IkeaOrderDetail ikeaOrderDetail) {
    List<Profile> profiles = ikeaOrderDetail.getIkeaClientOrderItemDtos().stream()
        .map(IkeaClientOrderItemDto::getProfile)
        .distinct()
        .collect(toList());
    profileListView.getItems().setAll(profiles);

    ikeaProcessOrderId = ikeaOrderDetail.getId();

    calculate(false);
  }

  private void calculate(boolean unload) {
    storageCalculationService.setIkeaProcessOrderId(ikeaProcessOrderId,
        profileListView.getItems().stream()
            .map(Profile::getId)
            .collect(Collectors.toList()),
        unload
    );
    storageCalculationService.restart();
  }

  private Node initLeftPane() {
    VBox leftPane = new VBox(0);

    profileListView = new ListView<>();
    profileListView.setCellFactory(param -> new ListCell<Profile>() {
      @Override
      protected void updateItem(Profile item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
          setText(item.getFirstName() + " " + item.getLastName());
        }
      }
    });
    profileListView.setMaxHeight(100);

    leftPane.getChildren().add(profileListView);

    BorderPane readyMainPane = new BorderPane();
    readyStockItemTableView = new StockItemTableView();
    readyMainPane.setCenter(readyStockItemTableView);

    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createWarehouse32Icon());
      button.setOnAction(event -> calculate(true));
      button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.UPLOAD_WAREHOUSE)));
      button.setDisable(true);

      readyStockItemTableView.itemsProperty().get().addListener((ListChangeListener<StockItemDto>) c -> button.setDisable(c.getList().size() == 0));

      toolBar.getItems().add(button);
    }

    readyMainPane.setBottom(toolBar);

    VBox.setVgrow(readyMainPane, Priority.ALWAYS);
    leftPane.getChildren().add(readyMainPane);

    return leftPane;
  }

  private Node initRightPane(DialogSupport dialogSupport) {
    TabPane tabPane = new TabPane();

    {
      Tab tab = new Tab(I18n.UA.getString(I18nKeys.OVER_BOUGH));

      overBoughTableView = new BaseTableView<>();
      overBoughTableView.setRowRenderListener((row, newValue) -> {
        row.setContextMenu(null);

        if (newValue != null) {
          ContextMenu contextMenu = new ContextMenu();

          {
            MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.ADD_TO_ORDER));
            menuItem.setOnAction(event -> {
              ChoiceCountDialog choiceCountDialog = new ChoiceCountDialog(dialogSupport.getStage());
              choiceCountDialog.maxValue(newValue.getItem().getArtNumber(), newValue.getItem().getInvoiceItemId(), profileListView.getItems(), newValue.getItem().getCount(), new EntityDialogCallback<NewOrderItemInfo>() {
                @Override
                public void onSave(NewOrderItemInfo newOrderItemInfo, Object... params) {
                  dialogSupport.hidePopupDialog();

                  addToOrderItemService.setChoiceCountResult(ikeaProcessOrderId, Collections.singletonList(newOrderItemInfo));
                  addToOrderItemService.restart();
                }

                @Override
                public void onCancel() {
                  dialogSupport.hidePopupDialog();
                }
              });

              dialogSupport.showPopupDialog(choiceCountDialog);
            });

            contextMenu.getItems().add(menuItem);
          }
          {
            MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.RETURN_BACK));
            menuItem.setOnAction(event -> Dialog.confirm(dialogSupport, I18n.UA.getString(I18nKeys.RETURN_BACK_INVOICE_ITEM_ORDER_QUESTION), new DialogCallback() {
              @Override
              public void onCancel() {

              }

              @Override
              public void onYes() {

              }
            }));
            contextMenu.getItems().add(menuItem);
          }

          {
            MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.ADD_TO_COMBO));
            menuItem.setOnAction(event -> {
              OrderViewComboDialog dialog = getOrderViewComboDialog(dialogSupport.getStage());

              dialog.loadCombos(ikeaProcessOrderId, newValue.getItem().getArtNumber(), new EntityDialogCallback<OrderViewComboDialog.ComboSelectResult>() {
                @Override
                public void onSave(OrderViewComboDialog.ComboSelectResult comboSelectResult, Object... params) {
                  addComboPartService.setData(comboSelectResult);
                  addComboPartService.restart();
                  dialogSupport.hidePopupDialog();
                }

                @Override
                public void onCancel() {
                  dialogSupport.hidePopupDialog();
                }
              });

              dialogSupport.showPopupDialog(dialog);

            });

            contextMenu.getItems().add(menuItem);
          }

          row.setContextMenu(contextMenu);
        }
      });

      {
        TableColumn<EntityCheckBoxHolder<StockItemDto>, Boolean> checked = new EntityCheckBoxTableColumn<>();
        checked.setCellValueFactory(new PropertyValueFactory<>("checked"));
        overBoughTableView.getColumns().add(checked);
      }

      {
        TableColumn<EntityCheckBoxHolder<StockItemDto>, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));

        column.setMinWidth(140);
        column.setCellValueFactory(ColumnUtil.column("item.artNumber"));
        column.setCellFactory(ArtNumberCell::new);
        overBoughTableView.getColumns().add(column);
      }

      {
        TableColumn<EntityCheckBoxHolder<StockItemDto>, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SHORT_NAME));

        column.setMinWidth(140);
        column.setCellValueFactory(ColumnUtil.column("item.shortName"));

        overBoughTableView.getColumns().add(column);
      }

      {
        TableColumn<EntityCheckBoxHolder<StockItemDto>, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));

        column.setMinWidth(60);
        column.setCellValueFactory(ColumnUtil.number("item.count"));

        overBoughTableView.getColumns().add(column);
      }

      ToolBar overBoughToolBar = new ToolBar();

      {
        SplitMenuButton button = new SplitMenuButton();
        button.setText("Actions");

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.ADD_CHECKED_TO_ORDER));
          menuItem.setOnAction(event -> {
            //TODO FIX ME SHOW DIALOG TO CHOOSE PROFILE
            Profile profile = profileListView.getItems().get(0);

            List<NewOrderItemInfo> newOrderItemInfos = overBoughTableView.getItems().stream()
                .filter(EntityCheckBoxHolder::isChecked)
                .map(stockItemDtoEntityCheckBoxHolder -> {
                  StockItemDto itemDto = stockItemDtoEntityCheckBoxHolder.getItem();

                  return new NewOrderItemInfo(profile.getId(), itemDto.getInvoiceItemId(), itemDto.getCount());
                }).collect(Collectors.toList());

            if (!newOrderItemInfos.isEmpty()) {
              Dialog.confirm(dialogSupport, I18n.UA.getString(I18nKeys.WARNING), I18n.UA.getString(I18nKeys.ALL_CHECKED_ITEMS_WILL_ADD_TO_ORDER_WITH_FULL_COUNT), new DialogCallback() {
                @Override
                public void onCancel() {
                  dialogSupport.hidePopupDialog();
                }

                @Override
                public void onYes() {
                  addToOrderItemService.setChoiceCountResult(ikeaProcessOrderId, newOrderItemInfos);
                  addToOrderItemService.restart();
                }
              });
            }
          });

          button.getItems().add(menuItem);

          overBoughToolBar.getItems().add(button);
        }
      }

      BorderPane main = new BorderPane();

      main.setTop(overBoughToolBar);
      main.setCenter(overBoughTableView);
      main.setBottom(overBoughTotalStatusPanel = new TotalStatusPanel());

      tab.setClosable(false);
      tab.setContent(main);


      tabPane.getTabs().add(tab);
    }
    {
      Tab tab = new Tab(I18n.UA.getString(I18nKeys.STORAGE_LACK));
      tab.setClosable(false);

      BorderPane main = new BorderPane();

      storageLackTableView = new StockItemTableView();

      main.setCenter(storageLackTableView);
      main.setBottom(storageLackTotalStatusPanel = new TotalStatusPanel());

      ToolBar toolBar = new ToolBar();

      {
        Button button = new Button(null, ImageFactory.createIkeaSmallIcon());
        button.setOnAction(event -> {
          IkeaSiteExportDialog dialog = getExportDialog(dialogSupport);

          Map<Long, List<StockItemDto>> groupedItems = storageLackTableView.getItems().stream().collect(Collectors.groupingBy(StockItemDto::getProfileId));

          List<IkeaClientOrderItemDto> data = groupedItems.entrySet().stream().map(entry -> {
            IkeaClientOrderItemDto ikeaClientOrderItemDto = new IkeaClientOrderItemDto();

            Optional<Profile> profileOptional = profileListView.getItems().stream().filter(profile -> profile.getId().equals(entry.getKey())).findFirst();

            if (profileOptional.isPresent()) {
              ikeaClientOrderItemDto.setProfile(profileOptional.get());

              ikeaClientOrderItemDto.setIkeaOrderItems(
                  entry.getValue().stream()
                      .map(itemDto -> {
                        IkeaOrderItem ikeaOrderItem = new IkeaOrderItem();

                        ikeaOrderItem.setCount(itemDto.getCount());

                        IkeaProduct ikeaProduct = new IkeaProduct();
                        ikeaProduct.setGroup(itemDto.getGroup());
                        ikeaProduct.setArtNumber(itemDto.getArtNumber());

                        ikeaOrderItem.setProduct(ikeaProduct);

                        return ikeaOrderItem;
                      })
                      .collect(Collectors.toList()));

              return ikeaClientOrderItemDto;
            } else {
              return null;
            }
          }).collect(Collectors.toList());


          dialog.bind(data);
          dialogSupport.showPopupDialog(dialog);
        });

        toolBar.getItems().add(button);
      }
      main.setTop(toolBar);
      tab.setContent(main);

      tabPane.getTabs().add(tab);
    }

    HBox.setHgrow(tabPane, Priority.ALWAYS);
    return tabPane;
  }

  private OrderViewComboDialog getOrderViewComboDialog(Stage stage) {
    if (orderViewComboDialog == null) {
      orderViewComboDialog = new OrderViewComboDialog(stage);
    }

    return orderViewComboDialog;
  }


  public IkeaSiteExportDialog getExportDialog(DialogSupport dialogSupport) {
    IkeaSiteExportDialog exportDialog = new IkeaSiteExportDialog(dialogSupport.getStage());
    exportDialog.setDefaultAction(dialog -> {
      dialog.setDefaultAction(null);
      dialogSupport.hidePopupDialog();
    });

    return exportDialog;
  }

}

class StorageCalculationService extends AbstractAsyncService<StorageCalculationResultDto> {
  private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();
  private ObjectProperty<List<Long>> profileIdsProperty = new SimpleObjectProperty<>();
  private BooleanProperty unloadingProperty = new SimpleBooleanProperty();

  @Override
  protected Task<StorageCalculationResultDto> createTask() {
    final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();
    final List<Long> _profileIds = profileIdsProperty.get();
    final boolean _unloading = unloadingProperty.get();

    return new Task<StorageCalculationResultDto>() {
      @Override
      protected StorageCalculationResultDto call() throws Exception {
        APIRequest apiRequest;

        if (!_unloading) {
          apiRequest = HttpServiceUtil.get("/ikea-order/storage/calculate/" + _ikeaProcessOrderId);
        } else {
          apiRequest = HttpServiceUtil.get("/ikea-order/storage/unload/" + _ikeaProcessOrderId);
        }

        return apiRequest.postData(_profileIds, StorageCalculationResultDto.class);
      }
    };
  }

  public void setUploading(boolean unloading) {
    unloadingProperty.set(unloading);
  }

  public void setIkeaProcessOrderId(Long ikeaProcessOrderId, List<Long> profileIds, boolean unloading) {
    ikeaProcessOrderIdProperty.set(ikeaProcessOrderId);
    profileIdsProperty.set(profileIds);
    setUploading(unloading);
  }


}

class IkeaProcessOrderResetStateService extends AbstractAsyncService<Boolean> {
  private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();

  @Override
  protected Task<Boolean> createTask() {
    final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();
    return new Task<Boolean>() {
      @Override
      protected Boolean call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/ikea-order/warehouse/" + _ikeaProcessOrderId + "/reset");
        return request.getData(Boolean.class);
      }
    };
  }

  public void setIkeaProcessOrderId(Long ikeaProcessOrderId) {
    this.ikeaProcessOrderIdProperty.setValue(ikeaProcessOrderId);
  }
}

class AddToOrderItemService extends AbstractAsyncService<Void> {
  private ObjectProperty<List<NewOrderItemInfo>> choiceCountResultProperty = new SimpleObjectProperty<>();
  private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();

  @Override
  protected Task<Void> createTask() {
    final List<NewOrderItemInfo> _newOrderItemInfo = choiceCountResultProperty.get();
    final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();
    return new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/ikea-order/order/item/add/" + _ikeaProcessOrderId);
        request.postData(_newOrderItemInfo);
        return null;
      }
    };
  }

  public void setChoiceCountResult(Long ikeaProcessOrderId, List<NewOrderItemInfo> newOrderItemInfos) {
    choiceCountResultProperty.setValue(newOrderItemInfos);
    ikeaProcessOrderIdProperty.setValue(ikeaProcessOrderId);
  }
}

class XlsExportInfo {
  private final List<StockItemDto> overBough;
  private final List<StockItemDto> lack;

  public XlsExportInfo(List<StockItemDto> overBough, List<StockItemDto> lack) {
    this.lack = lack;
    this.overBough = overBough;
  }

  public List<StockItemDto> getOverBough() {
    return overBough;
  }

  public List<StockItemDto> getLack() {
    return lack;
  }
}


