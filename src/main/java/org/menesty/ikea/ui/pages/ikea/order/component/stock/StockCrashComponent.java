package org.menesty.ikea.ui.pages.ikea.order.component.stock;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.jxls.common.Context;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.Invoice;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.InvoiceItem;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.InvoiceSearchItem;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StockCrashItem;
import org.menesty.ikea.ui.controls.BaseEntityDialog;
import org.menesty.ikea.ui.controls.form.*;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.ikea.order.component.service.AddStockCrashService;
import org.menesty.ikea.ui.pages.ikea.order.component.service.DeleteStockItemService;
import org.menesty.ikea.ui.pages.ikea.order.component.table.InvoiceItemSearchResultTableView;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 6/17/16.
 * 14:28.
 */
public abstract class StockCrashComponent extends BorderPane {
  private BaseTableView<StockCrashItem> stockCrashItemTableView;
  private StockCrashDialog stockCrashDialog;

  public StockCrashComponent(DialogSupport dialogSupport, AddStockCrashService addStockCrashService,
                             DeleteStockItemService deleteStockItemService) {
    stockCrashItemTableView = new BaseTableView<>();

    {
      TableColumn<StockCrashItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));

      column.setMinWidth(120);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);

      stockCrashItemTableView.getColumns().add(column);
    }

    {
      TableColumn<StockCrashItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));


      column.setCellValueFactory(ColumnUtil.number("count"));
      stockCrashItemTableView.getColumns().add(column);
    }

    {
      TableColumn<StockCrashItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.DEFECT_TYPE));

      column.setMinWidth(140);
      column.setCellValueFactory(param -> {
        if (param.getValue() != null) {
          return new SimpleStringProperty(I18n.UA.getString("product.defect.type." + param.getValue().getDefectType().toString().toLowerCase()));
        }

        return null;
      });
      stockCrashItemTableView.getColumns().add(column);
    }

    {
      TableColumn<StockCrashItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.LOCATION));

      column.setMinWidth(140);
      column.setCellValueFactory(param -> {
        if (param.getValue() != null) {
          return new SimpleStringProperty(I18n.UA.getString("location.place." + param.getValue().getLocationPlace().toString().toLowerCase()));
        }

        return null;
      });
      stockCrashItemTableView.getColumns().add(column);
    }

    stockCrashItemTableView.setRowRenderListener((row, newValue) -> {
      row.setContextMenu(null);

      if (newValue != null) {
        ContextMenu contextMenu = new ContextMenu();

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.DELETE));

          menuItem.setOnAction(event -> {
            deleteStockItemService.setData(DeleteStockItemService.StockAction.DELETE_STOCK_CRASH_ITEM, newValue);
            deleteStockItemService.restart();
          });

          contextMenu.getItems().add(menuItem);
        }

        row.setContextMenu(contextMenu);
      }
    });
    setCenter(stockCrashItemTableView);

    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createAdd32Icon());

      button.setOnAction(event -> {
        StockCrashDialog dialog = getStockCrashDialog(dialogSupport);
        dialog.setInvoices(getInvoices());
        dialog.bind(null, new EntityDialogCallback<StockCrashItem>() {
          @Override
          public void onSave(StockCrashItem stockCrashItem, Object... params) {
            addStockCrashService.setData(stockCrashItem);
            addStockCrashService.restart();
            dialogSupport.hidePopupDialog();
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

    setTop(toolBar);
  }


  public void setItems(List<StockCrashItem> items) {
    stockCrashItemTableView.getItems().setAll(items);
  }

  public abstract List<Invoice> getInvoices();

  private StockCrashDialog getStockCrashDialog(DialogSupport dialogSupport) {
    if (stockCrashDialog == null) {
      stockCrashDialog = new StockCrashDialog(dialogSupport.getStage());
    }

    return stockCrashDialog;
  }
}

class StockCrashDialog extends BaseEntityDialog<StockCrashItem> {
  private StockCrashForm form;

  public StockCrashDialog(Stage stage) {
    super(stage);

    addRow(form = new StockCrashForm());
    addRow(bottomBar);

    setMinWidth(600);
  }

  @Override
  protected StockCrashItem collect() {
    InvoiceSearchItem searchItem = form.getInvoiceItem();

    StockCrashItem crashItem = new StockCrashItem();

    crashItem.setArtNumber(searchItem.getArtNumber());
    crashItem.setProductId(searchItem.getProductId());
    crashItem.setLocationPlace(form.getLocationPlace());
    crashItem.setDefectType(form.getDefectType());
    crashItem.setPrice(searchItem.getPrice());
    crashItem.setIkeaProcessedOrderId(searchItem.getIkeaProcessedOrderId());
    crashItem.setCount(form.getCount());

    return crashItem;
  }

  @Override
  public boolean isValid() {
    return form.isValid();
  }

  @Override
  public void reset() {
    form.reset();
  }

  @Override
  protected void populate(StockCrashItem entityValue) {

  }

  public void setInvoices(List<Invoice> invoices) {
    form.items = invoices.stream()
        .map(invoice -> invoice.getInvoiceItems().stream().map(invoiceItem -> {
          InvoiceSearchItem item = InvoiceSearchItem.valueOf(invoiceItem);

          item.setInvoiceName(invoice.getInvoiceName());
          item.setParagonNumber(invoice.getParagonNumber());
          item.setIkeaProcessedOrderId(invoice.getIkeaProcessOrderId());

          return item;
        }).collect(Collectors.toList()))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    form.applyFilter(null);
  }

  class StockCrashForm extends FormPane {
    private InvoiceItemSearchResultTableView<InvoiceSearchItem> tableView;
    private NumberTextField countField;
    private List<InvoiceSearchItem> items;
    private ComboBoxField<TypeValue<StockCrashItem.DefectType>> defectTypeComboField;
    private ComboBoxField<TypeValue<StockCrashItem.LocationPlace>> locationPlaceComboField;

    public StockCrashForm() {
      add(countField = new NumberTextField(I18n.UA.getString(I18nKeys.COUNT), false));
      countField.setAllowDouble(false);
      countField.setMinValue(BigDecimal.ONE);

      add(defectTypeComboField = new ComboBoxField<>(I18n.UA.getString(I18nKeys.DEFECT_TYPE)));

      defectTypeComboField.setItems(
          Arrays.asList(StockCrashItem.DefectType.values()).stream()
              .map(item -> new TypeValue<>(item, I18n.UA.getString("product.defect.type." + item.toString().toLowerCase())))
              .collect(Collectors.toList())
      );
      defectTypeComboField.setAllowBlank(false);

      add(locationPlaceComboField = new ComboBoxField<>(I18n.UA.getString(I18nKeys.LOCATION)));

      locationPlaceComboField.setItems(
          Arrays.asList(StockCrashItem.LocationPlace.values()).stream()
              .map(item -> new TypeValue<>(item, I18n.UA.getString("location.place." + item.toString().toLowerCase())))
              .collect(Collectors.toList())
      );
      locationPlaceComboField.setAllowBlank(false);

      tableView = new InvoiceItemSearchResultTableView<>();
      ToolBar toolBar = new ToolBar();

      {
        TextField artNumber = new TextField();
        artNumber.setDelay(1);
        artNumber.setOnDelayAction(actionEvent -> applyFilter(artNumber.getText()));
        artNumber.setPromptText("Product ID #");

        toolBar.getItems().add(artNumber);
      }

      VBox vBox = new VBox();
      vBox.getChildren().addAll(toolBar, tableView);

      add(new WrapField<VBox>(null, vBox) {
        @Override
        public boolean isValid() {
          return tableView.getSelectionModel().getSelectedItem() != null;
        }

        @Override
        public void reset() {
          tableView.getSelectionModel().clearSelection();
        }
      }, 2);
    }

    private void applyFilter(String text) {
      List<InvoiceSearchItem> result = items;

      if (!StringUtils.isBlank(text)) {
        result = items.stream()
            .filter(invoiceSearchItem -> invoiceSearchItem.getArtNumber().startsWith(text)).collect(Collectors.toList());
      }
      tableView.getItems().setAll(result);
    }

    public BigDecimal getCount() {
      return countField.getNumber();
    }

    public InvoiceSearchItem getInvoiceItem() {
      return tableView.getSelectionModel().getSelectedItem();
    }

    public StockCrashItem.DefectType getDefectType() {
      return defectTypeComboField.getValue().getValue();
    }

    public StockCrashItem.LocationPlace getLocationPlace() {
      return locationPlaceComboField.getValue().getValue();
    }
  }
}

class TypeValue<T> {
  private T value;
  private String name;

  public TypeValue(T value, String name) {
    this.value = value;
    this.name = name;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}