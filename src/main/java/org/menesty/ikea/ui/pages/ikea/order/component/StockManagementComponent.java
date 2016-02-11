package org.menesty.ikea.ui.pages.ikea.order.component;

import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StockItemDto;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StorageCalculationResultDto;
import org.menesty.ikea.lib.domain.order.IkeaClientOrderItemDto;
import org.menesty.ikea.lib.domain.order.IkeaOrderDetail;
import org.menesty.ikea.lib.dto.ikea.order.NewOrderItemInfo;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.DialogCallback;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.ikea.order.component.service.AddComboPartService;
import org.menesty.ikea.ui.pages.ikea.order.component.table.StockItemTableView;
import org.menesty.ikea.ui.pages.ikea.order.dialog.ChoiceCountDialog;
import org.menesty.ikea.ui.pages.ikea.order.dialog.combo.OrderViewComboDialog;
import org.menesty.ikea.ui.pages.wizard.order.step.service.AbstractExportAsyncService;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
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
  private BaseTableView<StockItemDto> overBoughTableView;
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
      overBoughTableView.getItems().setAll(value.getOverBoughtStockItems());
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
      Button button = new Button(null, ImageFactory.createReload32Icon());
      button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.RESET)));
      button.setOnAction(event -> Dialog.confirm(dialogSupport, I18n.UA.getString(I18nKeys.WARNING), I18n.UA.getString(I18nKeys.CONFIRM_RESET_QUESTION), new DialogCallback() {
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
          xlsResultExportAsyncService.setParam(new XlsExportInfo(overBoughTableView.getItems(), storageLackTableView.getItems()));
          xlsResultExportAsyncService.restart();
        }
      });

      toolBar.getItems().add(button);
    }

    setTop(toolBar);

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
    VBox rightPane = new VBox(0);

    overBoughTableView = new BaseTableView<>();
    overBoughTableView.setRowRenderListener((row, newValue) -> {
      row.setContextMenu(null);

      if (newValue != null) {
        ContextMenu contextMenu = new ContextMenu();

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.ADD_TO_ORDER));
          menuItem.setOnAction(event -> {
            ChoiceCountDialog choiceCountDialog = new ChoiceCountDialog(dialogSupport.getStage());
            choiceCountDialog.maxValue(newValue.getArtNumber(), newValue.getInvoiceItemId(), profileListView.getItems(), newValue.getCount(), new EntityDialogCallback<NewOrderItemInfo>() {
              @Override
              public void onSave(NewOrderItemInfo newOrderItemInfo, Object... params) {
                dialogSupport.hidePopupDialog();

                addToOrderItemService.setChoiceCountResult(ikeaProcessOrderId, newOrderItemInfo);
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

            dialog.loadCombos(ikeaProcessOrderId, newValue.getArtNumber(), new EntityDialogCallback<OrderViewComboDialog.ComboSelectResult>() {
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


    overBoughTableView.setMaxHeight(250);

    {
      TableColumn<StockItemDto, Number> column = new TableColumn<>();
      column.setMaxWidth(40);
      column.setCellValueFactory(ColumnUtil.<StockItemDto>indexColumn());

      overBoughTableView.getColumns().add(column);
    }

    {
      TableColumn<StockItemDto, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));

      column.setMinWidth(140);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);
      overBoughTableView.getColumns().add(column);
    }

    {
      TableColumn<StockItemDto, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SHORT_NAME));

      column.setMinWidth(140);
      column.setCellValueFactory(ColumnUtil.column("shortName"));

      overBoughTableView.getColumns().add(column);
    }

    {
      TableColumn<StockItemDto, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));

      column.setMinWidth(60);
      column.setCellValueFactory(ColumnUtil.number("count"));

      overBoughTableView.getColumns().add(column);
    }

    storageLackTableView = new StockItemTableView();

    storageLackTotalStatusPanel = new TotalStatusPanel();
    overBoughTotalStatusPanel = new TotalStatusPanel();

    rightPane.getChildren().addAll(overBoughTableView, overBoughTotalStatusPanel, storageLackTableView, storageLackTotalStatusPanel);
    VBox.setVgrow(storageLackTableView, Priority.ALWAYS);

    HBox.setHgrow(rightPane, Priority.ALWAYS);
    return rightPane;
  }

  private OrderViewComboDialog getOrderViewComboDialog(Stage stage) {
    if (orderViewComboDialog == null) {
      orderViewComboDialog = new OrderViewComboDialog(stage);
    }

    return orderViewComboDialog;
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
  private ObjectProperty<NewOrderItemInfo> choiceCountResultProperty = new SimpleObjectProperty<>();
  private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();

  @Override
  protected Task<Void> createTask() {
    final NewOrderItemInfo _newOrderItemInfo = choiceCountResultProperty.get();
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

  public void setChoiceCountResult(Long ikeaProcessOrderId, NewOrderItemInfo newOrderItemInfo) {
    choiceCountResultProperty.setValue(newOrderItemInfo);
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


