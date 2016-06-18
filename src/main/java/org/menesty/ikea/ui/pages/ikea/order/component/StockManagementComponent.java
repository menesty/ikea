package org.menesty.ikea.ui.pages.ikea.order.component;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.Invoice;
import org.menesty.ikea.lib.domain.ikea.logistic.order.OrderPurchaseDifferentItem;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StockItemDto;
import org.menesty.ikea.lib.domain.order.IkeaClientOrderItemDto;
import org.menesty.ikea.lib.domain.order.IkeaOrderDetail;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.table.EntityCheckBoxHolder;
import org.menesty.ikea.ui.pages.DialogCallback;
import org.menesty.ikea.ui.pages.ikea.order.component.service.*;
import org.menesty.ikea.ui.pages.ikea.order.component.stock.OverBoughtComponent;
import org.menesty.ikea.ui.pages.ikea.order.component.stock.ReturnItemTableView;
import org.menesty.ikea.ui.pages.ikea.order.component.stock.StockCrashComponent;
import org.menesty.ikea.ui.pages.ikea.order.component.table.StockItemTableView;
import org.menesty.ikea.ui.pages.ikea.order.dialog.export.IkeaSiteExportDialog;
import org.menesty.ikea.ui.pages.wizard.order.step.service.AbstractExportAsyncService;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.FileChooserUtil;
import org.menesty.ikea.util.ToolTipUtil;

import java.io.File;
import java.math.BigDecimal;
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
  private OverBoughtComponent overBoughtComponent;
  private TableView<OrderPurchaseDifferentItem> orderPurchaseDifferentItemTableView;
  private TableView<StockItemDto> storageLackTableView;
  private ComboLackInfoComponent comboLackInfoComponent;
  private StorageCalculationService storageCalculationService;
  private AddToOrderItemService addToOrderItemService;
  private AddComboPartService addComboPartService;
  private IkeaProcessOrderResetStateService ikeaProcessOrderResetStateService;
  private ReturnItemService returnItemService;

  private TotalStatusPanel storageLackTotalStatusPanel;
  private ReturnItemTableView returnItemTableView;
  private StockCrashComponent stockCrashComponent;
  private IkeaOrderDetail ikeaOrderDetail;

  public StockManagementComponent(DialogSupport dialogSupport) {
    StackPane mainPane = new StackPane();


    //Storage calculation result service
    storageCalculationService = new StorageCalculationService();
    storageCalculationService.setOnSucceededListener(value -> {
      readyStockItemTableView.getItems().setAll(value.getReadyStockItems());
      overBoughtComponent.setItems(
          value.getOverBoughtStockItems().stream()
              .map(stockItemDto -> new EntityCheckBoxHolder<>(false, stockItemDto))
              .collect(Collectors.toList())
      );
      storageLackTableView.getItems().setAll(value.getLackStockItems());

      BigDecimal storageLackTotal = value.getLackStockItems().stream()
          .map(StockItemDto::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      storageLackTotalStatusPanel.setTotal(storageLackTotal);

      orderPurchaseDifferentItemTableView.getItems().setAll(value.getOrderPurchaseDifferentItems());

      returnItemTableView.getItems().setAll(value.getReturnedItems());
      stockCrashComponent.setItems(value.getStockCrashItems());

      comboLackInfoComponent.setCombos(value.getNotCompleteCombos());
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

    returnItemService = new ReturnItemService();
    returnItemService.setOnSucceededListener(value -> {
      if (value) {
        storageCalculationService.setUploading(false);
        storageCalculationService.restart();
      }
    });

    xlsResultExportAsyncService = new AbstractExportAsyncService<XlsExportInfo>() {
      @Override
      protected void export(File file, XlsExportInfo param) {
        ServiceFacade.getXlsExportService().exportBuyResult(file, param.getOverBough(), param.getLack());
      }
    };

    HBox groupPane = new HBox();
    groupPane.getChildren().addAll(initLeftPane(), initRightPane(dialogSupport));

    LoadingPane loadingPane = new LoadingPane();

    mainPane.getChildren().addAll(groupPane, loadingPane);

    loadingPane.bindTask(storageCalculationService, addToOrderItemService, addComboPartService, ikeaProcessOrderResetStateService, returnItemService);
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
          ikeaProcessOrderResetStateService.setIkeaProcessOrderId(ikeaOrderDetail.getId());
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
          xlsResultExportAsyncService.setParam(new XlsExportInfo(convert(overBoughtComponent.getItems()), storageLackTableView.getItems()));
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
    this.ikeaOrderDetail = ikeaOrderDetail;

    overBoughtComponent.setIkeaProcessOrderId(ikeaOrderDetail.getId());


    calculate(false);
  }

  private void calculate(boolean unload) {
    storageCalculationService.setIkeaProcessOrderId(ikeaOrderDetail.getId(),
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

      tab.setClosable(false);
      tab.setContent(overBoughtComponent = new OverBoughtComponent(dialogSupport, addToOrderItemService, addComboPartService, returnItemService) {
        @Override
        public List<Profile> getProfiles() {
          return profileListView.getItems();
        }
      });

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

    {
      Tab tab = new Tab(I18n.UA.getString(I18nKeys.INFO));
      tab.setClosable(false);

      orderPurchaseDifferentItemTableView = new TableView<>();

      {
        TableColumn<OrderPurchaseDifferentItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));

        column.setMinWidth(140);
        column.setCellValueFactory(ColumnUtil.column("artNumber"));
        column.setCellFactory(ArtNumberCell::new);
        orderPurchaseDifferentItemTableView.getColumns().add(column);
      }

      {
        TableColumn<OrderPurchaseDifferentItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ORDER_ITEM_COUNT));

        column.setMinWidth(130);
        column.setCellValueFactory(ColumnUtil.number("orderItemCount"));

        orderPurchaseDifferentItemTableView.getColumns().add(column);
      }

      {
        TableColumn<OrderPurchaseDifferentItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.INVOICE_ITEM_COUNT));

        column.setMinWidth(120);
        column.setCellValueFactory(ColumnUtil.number("invoiceOrderCount"));

        orderPurchaseDifferentItemTableView.getColumns().add(column);
      }

      tab.setContent(orderPurchaseDifferentItemTableView);
      tabPane.getTabs().add(tab);
    }

    {
      Tab tab = new Tab(I18n.UA.getString(I18nKeys.COMBO_LACK));

      tab.setClosable(false);
      tab.setContent(comboLackInfoComponent = new ComboLackInfoComponent());

      tabPane.getTabs().add(tab);
    }

    {
      Tab tab = new Tab(I18n.UA.getString(I18nKeys.RETURN_BACK_ITEMS));

      tab.setClosable(false);
      tab.setContent(returnItemTableView = new ReturnItemTableView());

      tabPane.getTabs().add(tab);
    }

    {
      Tab tab = new Tab(I18n.UA.getString(I18nKeys.STOCK_CRASHED));

      tab.setClosable(false);
      tab.setContent(stockCrashComponent = new StockCrashComponent(dialogSupport) {

        @Override
        public List<Invoice> getInvoices() {
          return ikeaOrderDetail.getInvoices();
        }
      });

      tabPane.getTabs().add(tab);
    }

    HBox.setHgrow(tabPane, Priority.ALWAYS);
    return tabPane;
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
