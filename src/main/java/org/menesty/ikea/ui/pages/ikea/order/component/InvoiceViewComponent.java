package org.menesty.ikea.ui.pages.ikea.order.component;

import com.google.common.base.Preconditions;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.EppInformation;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.Invoice;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.InvoiceItem;
import org.menesty.ikea.lib.domain.order.IkeaOrderDetail;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.pages.DialogCallback;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.ikea.order.component.service.IkeaProductUpdateService;
import org.menesty.ikea.ui.pages.ikea.order.component.table.InvoiceItemTableView;
import org.menesty.ikea.ui.pages.ikea.order.dialog.invoice.IkeaInvoiceUploadDialog;
import org.menesty.ikea.ui.pages.ikea.order.dialog.invoice.InvoiceAddEditDialog;
import org.menesty.ikea.ui.pages.ikea.order.dialog.invoice.InvoiceItemAddDialog;
import org.menesty.ikea.ui.pages.ikea.order.dialog.invoice.InvoiceItemChangeCountDialog;
import org.menesty.ikea.ui.pages.ikea.order.dialog.product.ProductEditDialog;
import org.menesty.ikea.util.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

/**
 * Created by Menesty on
 * 10/4/15.
 * 21:51.
 */
public class InvoiceViewComponent extends HBox {
  private CreateFakeOrderInvoice createFakeOrderInvoice;
  private ProductEditDialog productEditDialog;

  private TotalStatusPanel invoiceTotalStatusPanel;

  enum ItemAction {
    Delete, Add
  }

  public interface InvoiceActionListener {
    void onExport(List<Invoice> invoices);

    void onInvoiceDelete(Invoice invoice);

    void onInvoiceAdd(Invoice invoice);

    void onInvoiceItemAdd(InvoiceItem item);

    void onInvoiceItemDelete(InvoiceItem item);
  }

  private IkeaOrderDetail ikeaOrderDetail;

  private TableView<Invoice> invoiceTableView;
  private InvoiceItemTableView<InvoiceItem> invoiceItemTableView;

  private InvoiceItemTotalStatus invoiceItemStatusPanel;
  private InvoiceActionService invoiceActionService;
  private InvoiceItemActionService invoiceItemActionService;
  private InvoiceEppExportService invoiceEppExportService;
  private IkeaProductUpdateService ikeaProductUpdateService;

  private InvoiceItemChangeCountDialog invoiceItemChangeCountDialog;
  private InvoiceItemChangeCountService invoiceItemChangeCountService;

  public InvoiceViewComponent(DialogSupport dialogSupport, InvoiceActionListener invoiceActionListener) {
    Preconditions.checkNotNull(invoiceActionListener);

    getChildren().addAll(initLeftPanel(dialogSupport, invoiceActionListener), initRightPanel(dialogSupport, invoiceActionListener));
  }

  private Pane initLeftPanel(final DialogSupport dialogSupport, final InvoiceActionListener invoiceActionListener) {
    BorderPane leftPanel = new BorderPane();
    LoadingPane loadingPane = new LoadingPane();

    StackPane mainPanel = new StackPane();
    mainPanel.getChildren().addAll(leftPanel, loadingPane);

    HBox.setHgrow(mainPanel, Priority.ALWAYS);

    invoiceTableView = new TableView<>();

    {
      TableColumn<Invoice, Number> column = new TableColumn<>();
      column.setMaxWidth(45);
      column.setCellValueFactory(ColumnUtil.indexColumn());

      invoiceTableView.getColumns().add(column);
    }

    {
      TableColumn<Invoice, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.FILE_NAME));
      column.setPrefWidth(130);
      column.setCellValueFactory(ColumnUtil.column("fileName"));

      invoiceTableView.getColumns().add(column);
    }

    {
      TableColumn<Invoice, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.INVOICE_NAME));
      column.setPrefWidth(100);
      column.setCellValueFactory(ColumnUtil.column("invoiceName"));

      invoiceTableView.getColumns().add(column);
    }

    {
      TableColumn<Invoice, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PARAGON_NUMBER));
      column.setCellValueFactory(ColumnUtil.column("paragonNumber"));
      column.setPrefWidth(100);
      invoiceTableView.getColumns().add(column);
    }

    {
      TableColumn<Invoice, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SELL_DATE));
      column.setCellValueFactory(ColumnUtil.dateColumn("sellDate"));
      column.setPrefWidth(110);
      invoiceTableView.getColumns().add(column);
    }

    {
      TableColumn<Invoice, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.AMOUNT));
      column.setCellValueFactory(ColumnUtil.number("payed"));
      column.setPrefWidth(70);
      invoiceTableView.getColumns().add(column);
    }

    invoiceTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      invoiceItemTableView.getItems().clear();
      invoiceItemStatusPanel.setTotal(BigDecimal.ZERO);
      invoiceItemStatusPanel.setInvoicePrice(BigDecimal.ZERO);

      if (newValue != null && newValue.getInvoiceItems() != null) {
        invoiceItemTableView.getItems().addAll(newValue.getInvoiceItems());
        invoiceItemStatusPanel.setTotal(newValue.getInvoiceItems().stream().map(InvoiceItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        invoiceItemStatusPanel.setInvoicePrice(newValue.getPayed());
      }
    });

    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createAdd32Icon());

      button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.ADD)));
      button.setOnAction(event -> {
        InvoiceAddEditDialog dialog = getInvoiceAddEditDialog(dialogSupport);

        Invoice invoice = new Invoice();
        invoice.setIkeaProcessOrderId(ikeaOrderDetail.getId());

        dialog.bind(invoice, new EntityDialogCallback<Invoice>() {
          @Override
          public void onSave(Invoice invoice, Object... params) {
            dialogSupport.hidePopupDialog();
            invoiceActionService.setInvoice(invoice, ItemAction.Add);
            invoiceActionService.restart();
          }

          @Override
          public void onCancel() {
            dialogSupport.hidePopupDialog();
          }
        });

        dialogSupport.showPopupDialog(dialog);
      });

      toolBar.getItems().add(button);
    }

    {
      Button button = new Button(null, ImageFactory.createPdf32Icon());
      button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.INVOICES_UPLOAD_PDF)));
      button.setOnAction(event -> {
        IkeaInvoiceUploadDialog dialog = getInvoiceUploadDialog(dialogSupport);
        dialog.bind(ikeaOrderDetail.getId(), invoiceActionListener);
        dialogSupport.showPopupDialog(dialog);
      });

      toolBar.getItems().add(button);
    }

    {
      Button button = new Button(null, ImageFactory.createDelete32Icon());

      button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.DELETE)));
      button.setOnAction(event -> {
        Dialog.confirm(dialogSupport, I18n.UA.getString(I18nKeys.DELETE_TITLE), I18n.UA.getString(I18nKeys.DELETE_CONFIRMATION_MESSAGE), new DialogCallback() {
          @Override
          public void onCancel() {

          }

          @Override
          public void onYes() {
            invoiceActionService.setInvoice(invoiceTableView.getSelectionModel().getSelectedItem(), ItemAction.Delete);
            invoiceActionService.restart();
          }
        });

      });
      button.setDisable(true);

      invoiceTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> button.setDisable(newValue == null));

      toolBar.getItems().add(button);
    }
    {
      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      toolBar.getItems().add(spacer);
    }

    {

      Button button = new Button(null, ImageFactory.createPdf32Icon());
      button.setTooltip(new Tooltip(I18n.UA.getString(I18nKeys.CREATE_FAKE_INVOICE_PDF)));
      button.setOnAction(actionEvent -> Dialog.confirm(dialogSupport, "Are you sure to create Fake items", new DialogCallback() {
        @Override
        public void onCancel() {
        }

        @Override
        public void onYes() {
          createFakeOrderInvoice.setIkeaProcessOrderId(ikeaOrderDetail.getId());
          createFakeOrderInvoice.restart();
        }
      }));

      toolBar.getItems().add(button);

    }

    invoiceTotalStatusPanel = new TotalStatusPanel();

    leftPanel.setTop(toolBar);
    leftPanel.setBottom(invoiceTotalStatusPanel);

    invoiceItemChangeCountService = new InvoiceItemChangeCountService();
    invoiceItemChangeCountService.setOnSucceededListener(value -> {
      if (value) {
        InvoiceItem invoiceItem = invoiceItemChangeCountService.invoiceItemProperty.get();
        invoiceItem.setCount(invoiceItemChangeCountService.itemCountProperty.get());

        invoiceItemTableView.update(invoiceItem);
      }
    });
    createFakeOrderInvoice = new CreateFakeOrderInvoice();
    createFakeOrderInvoice.setOnSucceededListener(value -> invoiceActionListener.onExport(Collections.singletonList(value)));

    invoiceActionService = new InvoiceActionService();
    invoiceActionService.setOnSucceededListener(result -> {
      if (ItemAction.Delete == result.getAction()) {
        invoiceActionListener.onInvoiceDelete(result.getItem());
      } else if (ItemAction.Add == result.getAction()) {
        invoiceActionListener.onInvoiceAdd(result.getItem());
      }
    });
    loadingPane.bindTask(invoiceActionService, createFakeOrderInvoice, invoiceItemChangeCountService);

    leftPanel.setCenter(invoiceTableView);

    return mainPanel;
  }

  public InvoiceItemChangeCountDialog getInvoiceItemChangeCountDialog(DialogSupport dialogSupport) {
    if (invoiceItemChangeCountDialog == null) {
      invoiceItemChangeCountDialog = new InvoiceItemChangeCountDialog(dialogSupport.getStage());
    }

    return invoiceItemChangeCountDialog;
  }

  private Node initRightPanel(DialogSupport dialogSupport, final InvoiceActionListener invoiceActionListener) {
    BorderPane rightPanel = new BorderPane();

    LoadingPane loadingPane = new LoadingPane();

    StackPane mainPanel = new StackPane();
    mainPanel.getChildren().addAll(rightPanel, loadingPane);

    HBox.setHgrow(mainPanel, Priority.ALWAYS);

    invoiceItemTableView = new InvoiceItemTableView<>();

    invoiceItemTableView.setRowRenderListener((row, newValue) -> {
      row.setContextMenu(null);

      if (newValue != null && newValue.getProductId() != null) {
        ContextMenu contextMenu = new ContextMenu();

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.EDIT), ImageFactory.createEdit16Icon());
          menuItem.setOnAction(event -> {
            IkeaProduct ikeaProduct = newValue.getProduct();

            if (ikeaProduct == null) {
              ikeaProduct = new IkeaProduct();
              ikeaProduct.setId(newValue.getProductId());
            }

            ProductEditDialog productEditDialog = getProductEditDialog(dialogSupport.getStage());
            productEditDialog.bind(ikeaProduct, new EntityDialogCallback<IkeaProduct>() {
              @Override
              public void onSave(IkeaProduct ikeaProduct, Object... params) {
                newValue.setProduct(ikeaProduct);
                newValue.setShortName(ikeaProduct.getShortName());

                invoiceItemTableView.update(newValue);
                dialogSupport.hidePopupDialog();

                ikeaProductUpdateService.setIkeaProduct(ikeaProduct);
                ikeaProductUpdateService.restart();
              }

              @Override
              public void onCancel() {
                dialogSupport.hidePopupDialog();
              }
            });

            dialogSupport.showPopupDialog(productEditDialog);
          });

          contextMenu.getItems().add(menuItem);
        }

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.CHANGE_ITEM_COUNT));
          menuItem.setOnAction(event -> {
            InvoiceItemChangeCountDialog invoiceItemChangeCountDialog = getInvoiceItemChangeCountDialog(dialogSupport);

            invoiceItemChangeCountDialog.bind(newValue.getCount(), new EntityDialogCallback<BigDecimal>() {
              @Override
              public void onSave(BigDecimal count, Object... params) {
                dialogSupport.hidePopupDialog();
                invoiceItemChangeCountService.setInvoiceItem(newValue, count);
                invoiceItemChangeCountService.restart();
              }

              @Override
              public void onCancel() {
                dialogSupport.hidePopupDialog();
              }
            });

            dialogSupport.showPopupDialog(invoiceItemChangeCountDialog);
          });

          contextMenu.getItems().add(menuItem);
        }

        row.setContextMenu(contextMenu);
      }
    });


    rightPanel.setCenter(invoiceItemTableView);
    rightPanel.setBottom(invoiceItemStatusPanel = new InvoiceItemTotalStatus());

    ToolBar toolBar = new ToolBar();
    {
      Button button = new Button(null, ImageFactory.createAdd32Icon());
      button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.ADD)));
      button.setOnAction(event -> {
        InvoiceItemAddDialog dialog = getInvoiceItemAddEditDialog(dialogSupport);

        InvoiceItem invoiceItem = new InvoiceItem();
        invoiceItem.setInvoiceId(invoiceTableView.getSelectionModel().getSelectedItem().getId());

        dialog.bind(invoiceItem, new EntityDialogCallback<InvoiceItem>() {
          @Override
          public void onSave(InvoiceItem invoiceItem, Object... params) {
            dialogSupport.hidePopupDialog();
            invoiceItemActionService.setInvoiceItem(invoiceItem, ItemAction.Add);
            invoiceItemActionService.restart();
          }

          @Override
          public void onCancel() {
            dialogSupport.hidePopupDialog();
          }
        });

        dialogSupport.showPopupDialog(dialog);
      });

      toolBar.getItems().add(button);
    }

    {
      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      toolBar.getItems().add(spacer);
    }

    {
      Button button = new Button(null, ImageFactory.createEppExport32Icon());
      button.setTooltip(new Tooltip(I18n.UA.getString(I18nKeys.EPP_EXPORT)));
      button.setOnAction(actionEvent -> {
        Invoice invoice = invoiceTableView.getSelectionModel().selectedItemProperty().get();

        String fileName = StringUtils.isNotBlank(invoice.getInvoiceName()) ?
            invoice.getInvoiceName().replaceAll("[/-]", "_") : ikeaOrderDetail.getName().replaceAll("[/-]", "_");

        FileChooser fileChooser = FileChooserUtil.getEpp(FileChooserUtil.FolderType.INVOICE);
        fileChooser.setInitialFileName(fileName + ".epp");

        File selectedFile = fileChooser.showSaveDialog(dialogSupport.getStage());

        if (selectedFile != null) {
          FileChooserUtil.setDefaultDir(FileChooserUtil.FolderType.INVOICE, selectedFile);
          invoiceEppExportService.setInvoiceId(selectedFile, invoice.getId(), ikeaOrderDetail.getName(), invoice.getInvoiceName());
          invoiceEppExportService.restart();
        }
      });

      invoiceTableView.getSelectionModel()
          .selectedItemProperty()
          .addListener(
              (observable, oldValue, newValue) -> {
                button.setDisable(newValue == null || (newValue.getParentInvoiceId() != null && newValue.getParentInvoiceId() != 0));
              }
          );
      toolBar.getItems().add(button);
    }


    toolBar.setDisable(true);
    invoiceTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> toolBar.setDisable(newValue == null));

    rightPanel.setTop(toolBar);

    invoiceItemActionService = new InvoiceItemActionService();
    invoiceItemActionService.setOnSucceededListener(result -> {
      if (ItemAction.Delete == result.getAction()) {
        invoiceActionListener.onInvoiceItemDelete(result.getItem());
      } else if (ItemAction.Add == result.getAction()) {
        invoiceActionListener.onInvoiceItemAdd(result.getItem());
      }
    });

    invoiceEppExportService = new InvoiceEppExportService();

    ikeaProductUpdateService = new IkeaProductUpdateService();

    loadingPane.bindTask(invoiceItemActionService, invoiceEppExportService, ikeaProductUpdateService);

    return mainPanel;
  }


  private ProductEditDialog getProductEditDialog(Stage stage) {
    if (productEditDialog == null) {
      productEditDialog = new ProductEditDialog(stage);
    }

    return productEditDialog;
  }

  private IkeaInvoiceUploadDialog getInvoiceUploadDialog(DialogSupport dialogSupport) {
    IkeaInvoiceUploadDialog invoiceUploadDialog = new IkeaInvoiceUploadDialog(dialogSupport.getStage());
    invoiceUploadDialog.setDefaultAction(baseDialog -> {
          baseDialog.setDefaultAction(null);
          dialogSupport.hidePopupDialog();
        }
    );

    return invoiceUploadDialog;
  }

  private InvoiceAddEditDialog getInvoiceAddEditDialog(DialogSupport dialogSupport) {
    return new InvoiceAddEditDialog(dialogSupport.getStage());
  }

  private InvoiceItemAddDialog getInvoiceItemAddEditDialog(DialogSupport dialogSupport) {
    return new InvoiceItemAddDialog(dialogSupport.getStage());
  }

  public void setIkeaOrderDetail(IkeaOrderDetail ikeaOrderDetail) {
    this.ikeaOrderDetail = ikeaOrderDetail;
  }

  public void setInvoices(List<Invoice> invoices) {
    invoiceTableView.getItems().setAll(invoices);

    BigDecimal totalCount = invoices.stream()
        .map(Invoice::getPayed)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    invoiceTotalStatusPanel.setTotal(totalCount);
  }

  class InvoiceActionService extends AbstractAsyncService<ItemActionResult<Invoice>> {
    private ObjectProperty<Invoice> invoiceProperty = new SimpleObjectProperty<>();
    private ObjectProperty<ItemAction> invoiceActionProperty = new SimpleObjectProperty<>();

    @Override
    protected Task<ItemActionResult<Invoice>> createTask() {
      final Invoice _invoice = invoiceProperty.get();
      final ItemAction _itemAction = invoiceActionProperty.get();

      return new Task<ItemActionResult<Invoice>>() {
        @Override
        protected ItemActionResult<Invoice> call() throws Exception {
          if (ItemAction.Delete == _itemAction) {
            APIRequest apiRequest = HttpServiceUtil.get("/ikea-order/invoice/delete/" + _invoice.getId());
            apiRequest.get();
          } else if (ItemAction.Add == _itemAction) {
            APIRequest apiRequest = HttpServiceUtil.get("/ikea-order/invoice/add/" + ikeaOrderDetail.getId());
            Long id = apiRequest.postData(_invoice, Long.class);
            _invoice.setId(id);
          }
          return new ItemActionResult<>(_invoice, _itemAction);
        }
      };
    }

    public void setInvoice(Invoice invoice, ItemAction action) {
      Preconditions.checkNotNull(invoice);
      Preconditions.checkNotNull(action);

      invoiceProperty.set(invoice);
      invoiceActionProperty.set(action);
    }
  }

  class InvoiceItemActionService extends AbstractAsyncService<ItemActionResult<InvoiceItem>> {
    private ObjectProperty<InvoiceItem> itemProperty = new SimpleObjectProperty<>();
    private ObjectProperty<ItemAction> itemActionProperty = new SimpleObjectProperty<>();

    @Override
    protected Task<ItemActionResult<InvoiceItem>> createTask() {
      final InvoiceItem _item = itemProperty.get();
      final ItemAction _itemAction = itemActionProperty.get();

      return new Task<ItemActionResult<InvoiceItem>>() {
        @Override
        protected ItemActionResult<InvoiceItem> call() throws Exception {
          if (ItemAction.Delete == _itemAction) {
            APIRequest apiRequest = HttpServiceUtil.get("/ikea-order/invoice-item/delete/" + _item.getId());
            apiRequest.get();
          } else if (ItemAction.Add == _itemAction) {
            APIRequest apiRequest = HttpServiceUtil.get("/ikea-order/invoice-item/add/" + _item.getInvoiceId());
            Long id = apiRequest.postData(_item, Long.class);
            _item.setId(id);
          }
          return new ItemActionResult<>(_item, _itemAction);
        }
      };
    }

    public void setInvoiceItem(InvoiceItem item, ItemAction action) {
      itemProperty.set(item);
      itemActionProperty.set(action);
    }
  }

  class InvoiceEppExportService extends AbstractAsyncService<Void> {
    private LongProperty invoiceIdProperty = new SimpleLongProperty();
    private ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();
    private StringProperty orderNameProperty = new SimpleStringProperty();
    private StringProperty invoiceNameProperty = new SimpleStringProperty();

    @Override
    protected Task<Void> createTask() {
      final Long _invoiceId = invoiceIdProperty.get();
      final File _file = fileProperty.get();
      final String _orderName = orderNameProperty.get();
      final String _invoiceName = invoiceNameProperty.get();

      return new Task<Void>() {
        @Override
        protected Void call() throws Exception {

          EppInformation eppInformation = new EppInformation();

          eppInformation.setFileName(_file.getName());
          eppInformation.setEppType(EppInformation.EppType.Invoice);
          eppInformation.setId(_invoiceId);
          eppInformation.setOrderName(_orderName);
          eppInformation.setInvoiceName(_invoiceName);

          APIRequest request = HttpServiceUtil.get("/epp/generate", eppInformation.getParams());
          URL url = request.getUrl().toURL();

          URLConnection con = url.openConnection();

          BufferedInputStream bis = new BufferedInputStream(con.getInputStream());

          Files.copy(bis, _file.toPath(), StandardCopyOption.REPLACE_EXISTING);

          return null;
        }
      };
    }

    public void setInvoiceId(File file, Long invoiceId, String orderName, String invoiceName) {
      fileProperty.setValue(file);
      invoiceIdProperty.setValue(invoiceId);
      orderNameProperty.setValue(orderName);
      invoiceNameProperty.setValue(invoiceName);
    }

    public File getFile() {
      return fileProperty.get();
    }
  }

  class CreateFakeOrderInvoice extends AbstractAsyncService<Invoice> {
    private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();

    @Override
    protected Task<Invoice> createTask() {
      final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();
      return new Task<Invoice>() {
        @Override
        protected Invoice call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/ikea-order/" + _ikeaProcessOrderId + "/fake/invoice");
          return request.getData(Invoice.class);
        }
      };
    }

    public void setIkeaProcessOrderId(Long ikeaProcessOrderId) {
      ikeaProcessOrderIdProperty.setValue(ikeaProcessOrderId);
    }
  }

  class InvoiceItemChangeCountService extends AbstractAsyncService<Boolean> {
    private ObjectProperty<InvoiceItem> invoiceItemProperty = new SimpleObjectProperty<>();
    private ObjectProperty<BigDecimal> itemCountProperty = new SimpleObjectProperty<>();

    @Override
    protected Task<Boolean> createTask() {
      final Long _id = invoiceItemProperty.get().getId();
      final BigDecimal _itemCount = itemCountProperty.get();

      return new Task<Boolean>() {
        @Override
        protected Boolean call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/ikea-order/invoice-item/" + _id + "/change/count/" + _itemCount.toString());

          return request.postData((Object) null, Boolean.class);
        }
      };
    }

    public void setInvoiceItem(InvoiceItem invoiceItem, BigDecimal itemCount) {
      invoiceItemProperty.setValue(invoiceItem);
      itemCountProperty.setValue(itemCount);
    }
  }


  public void setSelected(Invoice selected) {
    if (selected.equals(invoiceTableView.getSelectionModel().getSelectedItem())) {
      invoiceTableView.getSelectionModel().select(null);
    }

    invoiceTableView.getSelectionModel().select(selected);
  }

  class ItemActionResult<T> {
    private final T item;
    private final ItemAction action;


    ItemActionResult(T item, ItemAction action) {
      this.item = item;
      this.action = action;
    }

    public T getItem() {
      return item;
    }

    public ItemAction getAction() {
      return action;
    }
  }

  class InvoiceItemTotalStatus extends TotalStatusPanel {

    private Label priceDiff;

    public InvoiceItemTotalStatus() {
      super();
      {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getItems().add(spacer);
      }

      getItems().add(priceDiff = new Label());
    }

    public void setInvoicePrice(BigDecimal invoicePrice) {
      priceDiff.setText(getTotal().subtract(invoicePrice).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
    }
  }
}