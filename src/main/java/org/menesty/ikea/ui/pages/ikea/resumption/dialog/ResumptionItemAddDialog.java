package org.menesty.ikea.ui.pages.ikea.resumption.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.beans.property.SimpleBigDecimalProperty;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.Invoice;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.ResumptionItem;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.ResumptionItemStatus;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.CustomTotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.form.*;
import org.menesty.ikea.ui.controls.form.provider.AsyncFilterDataProvider;
import org.menesty.ikea.ui.controls.form.provider.FilterAsyncService;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.ui.table.NumericEditableTableCell;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.DateUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 3/10/16.
 * 08:45.
 */
public class ResumptionItemAddDialog extends BaseDialog {
  private ResumptionItemForm resumptionItemForm;
  private LoadService loadService;
  private AddResumptionService addResumptionService;
  private CustomTotalStatusPanel totalStatusPanel;


  public ResumptionItemAddDialog(Stage stage) {
    super(stage);
    setAllowAutoHide(false);
    setMinWidth(500);
    setTitle(I18n.UA.getString(I18nKeys.RESUMPTION_CORRECTION_TITLE));
    addRow(resumptionItemForm = new ResumptionItemForm());
    addRow(bottomBar);

    cancelBtn.setText(I18n.UA.getString(I18nKeys.CLOSE));
    okBtn.setText(I18n.UA.getString(I18nKeys.ADD));

    addResumptionService = new AddResumptionService();
    addResumptionService.setOnSucceededListener(value -> {
      totalStatusPanel.setCurrentTotal(BigDecimal.ZERO);
      totalStatusPanel.setTotal(BigDecimal.ZERO);
      resumptionItemForm.reset();
    });

    loadingPane.bindTask(loadService, addResumptionService);

  }


  @Override
  public void onOk() {
    if (resumptionItemForm.isValid()) {
      List<ResumptionItem> items = resumptionItemForm.getItems().stream()
          .filter(resumptionItemStatusModel ->
              resumptionItemStatusModel.countProperty().get() != null && resumptionItemStatusModel.countProperty().get().compareTo(BigDecimal.ZERO) != 0)
          .map(resumptionItemStatusModel -> {
            ResumptionItem resumptionItem = new ResumptionItem();

            resumptionItem.setCount(resumptionItemStatusModel.countProperty.get());
            resumptionItem.setInvoiceItemId(resumptionItemStatusModel.getItem().getInvoiceItemId());
            resumptionItem.setArtNumber(resumptionItemStatusModel.getItem().getArtNumber());
            resumptionItem.setProductId(resumptionItemStatusModel.getItem().getProductId());
            resumptionItem.setInvoiceId(resumptionItemStatusModel.getItem().getInvoiceId());

            return resumptionItem;
          })
          .collect(Collectors.toList());

      Date resumptionDate = resumptionItemForm.getResumptionDate();
      Long resumptionInvoice = resumptionItemForm.getResumptionInvoice();
      Integer resumptionShop = resumptionItemForm.getResumptionShop();

      items.stream().forEach(resumptionItem -> {
        resumptionItem.setResumptionDate(resumptionDate);
        resumptionItem.setResumptionInvoice(resumptionInvoice);
        resumptionItem.setResumptionShop(resumptionShop);
      });

      addResumptionService.resumptionItemsProperty.setValue(items);
      addResumptionService.restart();
    }
  }

  @Override
  public void onShow() {
    resumptionItemForm.reset();
  }

  class ResumptionItemForm extends FormPane {
    private DatePicker createdDateField;
    private NumberTextField shopFiled;
    private NumberTextField invoiceNumber;
    private ComboBoxField<Invoice> invoiceListViewField;
    private TableView<ResumptionItemStatusModel> tableView;


    private List<ResumptionItemStatusModel> resumptionItemStatuses = new ArrayList<>();

    public ResumptionItemForm() {
      invoiceListViewField = new ComboBoxField<>(I18n.UA.getString(I18nKeys.INVOICE_NAME));
      invoiceListViewField.setItemLabel(Invoice::getInvoiceName);
      invoiceListViewField.setEditable(true);
      invoiceListViewField.setAllowBlank(false);
      invoiceListViewField.setLoader(new AsyncFilterDataProvider<>(new FilterAsyncService<List<Invoice>>() {
        @Override
        public Task<List<Invoice>> createTask(String queryString) {
          return new Task<List<Invoice>>() {
            @Override
            protected List<Invoice> call() throws Exception {
              Map<String, String> map = new HashMap<>();
              map.put("queryString", queryString);

              APIRequest apiRequest = HttpServiceUtil.get("/invoice/find", map);

              return apiRequest.getList(new TypeReference<List<Invoice>>() {
              });
            }
          };
        }
      }), 3);


      invoiceListViewField.addSelectItemListener((observable, oldValue, newValue) -> {
        if (newValue != null) {
          loadService.invoiceIdProperty.setValue(newValue.getId());
          loadService.restart();
        }
      });


      add(invoiceListViewField);

      add(new WrapField<DatePicker>(I18n.UA.getString(I18nKeys.RESUMPTION_DATE), createdDateField = new DatePicker()) {
        @Override
        public boolean isValid() {
          return node.getValue() != null;
        }

        @Override
        public void reset() {
          createdDateField.setValue(null);
        }
      });

      HBox invoiceName = new HBox();

      invoiceName.getChildren().add(shopFiled = new NumberTextField(I18n.UA.getString(I18nKeys.SHORT_NAME), false));
      invoiceName.getChildren().add(invoiceNumber = new NumberTextField(I18n.UA.getString(I18nKeys.INVOICE_NAME), false));

      invoiceNumber.addValidationRule(value -> value != null && !value.equals(BigDecimal.ZERO));

      shopFiled.setMaxWidth(60);
      shopFiled.setAllowDouble(false);
      invoiceNumber.setAllowDouble(false);

      add(new WrapField<HBox>(I18n.UA.getString(I18nKeys.RESUMPTION_INVOICE), invoiceName) {
        @Override
        public boolean isValid() {
          return shopFiled.isValid() && invoiceNumber.isValid();
        }

        @Override
        public void reset() {
          shopFiled.setNumber(BigDecimal.valueOf(204));
          invoiceNumber.reset();
        }
      });

      tableView = new TableView<>();
      tableView.setEditable(true);

      {
        TableColumn<ResumptionItemStatusModel, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
        column.setMinWidth(140);
        column.setCellValueFactory(ColumnUtil.column("item.artNumber"));
        column.setCellFactory(ArtNumberCell::new);

        tableView.getColumns().add(column);
      }

      {
        TableColumn<ResumptionItemStatusModel, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
        column.setMinWidth(60);
        column.setCellValueFactory(ColumnUtil.column("item.price"));

        tableView.getColumns().add(column);
      }

      {
        TableColumn<ResumptionItemStatusModel, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));
        column.setCellValueFactory(ColumnUtil.number("item.available"));
        column.setMinWidth(60);
        tableView.getColumns().add(column);
      }

      {
        TableColumn<ResumptionItemStatusModel, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.RESUMPTION_COUNT));
        column.setCellValueFactory(new PropertyValueFactory<>("count"));
        column.setMinWidth(140);
        column.setCellFactory(param -> new NumericEditableTableCell<ResumptionItemStatusModel>() {
          @Override
          public boolean isValid(BigDecimal value) {
            ResumptionItemStatusModel item = (ResumptionItemStatusModel) getTableRow().getItem();

            return value != null && value.compareTo(item.getItem().getAvailable()) < 1;
          }
        });
        tableView.getColumns().add(column);
      }

      tableView.setMinHeight(160);


      ToolBar toolBar = new ToolBar();
      TextField artNumber = new TextField();

      {
        artNumber.setDelay(1);
        artNumber.setOnDelayAction(actionEvent -> applyFilter(artNumber.getText()));
        artNumber.setPromptText("Product ID #");

        toolBar.getItems().add(artNumber);
      }

      loadService = new LoadService();
      loadService.setOnSucceededListener(value -> {
        resumptionItemStatuses = value.stream()
            .map(ResumptionItemStatusModel::new)
            .collect(Collectors.toList());

        resumptionItemStatuses.forEach(resumptionItemStatusModel -> resumptionItemStatusModel.countProperty().addListener((observable, oldValue, newValue) -> {
          List<ResumptionItemStatusModel> filtered = resumptionItemStatuses.stream()
              .filter(rism -> rism.countProperty().get() != null && rism.countProperty().get().compareTo(BigDecimal.ZERO) != 0)
              .collect(Collectors.toList());

          BigDecimal totalPrice = filtered.stream()
              .map(rism -> rism.getItem().getPrice().multiply(rism.countProperty().get()))
              .reduce(BigDecimal.ZERO, BigDecimal::add);

          totalStatusPanel.setCurrentTotal(new BigDecimal(filtered.size()));
          totalStatusPanel.setTotal(totalPrice);
        }));

        tableView.getItems().setAll(resumptionItemStatuses);
      });

      BorderPane main = new BorderPane();
      main.setTop(toolBar);
      main.setCenter(tableView);

      add(new WrapField<BorderPane>(null, main) {
        @Override
        public boolean isValid() {
          return true;
        }

        @Override
        public void reset() {
          artNumber.reset();
          resumptionItemStatuses.clear();
          tableView.getItems().setAll(resumptionItemStatuses);
        }
      }, 2);

      main.setBottom(totalStatusPanel = new CustomTotalStatusPanel(I18n.UA.getString(I18nKeys.COUNT) + " :"));
    }

    private void applyFilter(String artNumber) {
      List<ResumptionItemStatusModel> items;
      if (StringUtils.isNotBlank(artNumber)) {
        items = resumptionItemStatuses.stream()
            .filter(entity -> entity.getItem().getArtNumber().contains(artNumber))
            .collect(Collectors.toList());
      } else {
        items = resumptionItemStatuses;
      }

      tableView.getItems().setAll(items);
    }

    public List<ResumptionItemStatusModel> getItems() {
      return resumptionItemStatuses;
    }

    public Date getResumptionDate() {
      return DateUtil.toDate(createdDateField.getValue());
    }

    public Integer getResumptionShop() {
      return shopFiled.getNumber().intValue();
    }

    public Long getResumptionInvoice() {
      return invoiceNumber.getNumber().longValue();
    }
  }

  class LoadService extends AbstractAsyncService<List<ResumptionItemStatus>> {
    private LongProperty invoiceIdProperty = new SimpleLongProperty();

    @Override
    protected Task<List<ResumptionItemStatus>> createTask() {
      Long _invoiceId = invoiceIdProperty.get();

      return new Task<List<ResumptionItemStatus>>() {
        @Override
        protected List<ResumptionItemStatus> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/resumption/items/available/invoice/" + _invoiceId);

          return request.getList(new TypeReference<List<ResumptionItemStatus>>() {
          });
        }
      };
    }
  }

  class AddResumptionService extends AbstractAsyncService<Void> {
    private ObjectProperty<List<ResumptionItem>> resumptionItemsProperty = new SimpleObjectProperty<>();

    @Override
    protected Task<Void> createTask() {
      final List<ResumptionItem> _resumptionItems = resumptionItemsProperty.get();

      return new Task<Void>() {
        @Override
        protected Void call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/resumption/items/add");

          request.postData(_resumptionItems);
          return null;
        }
      };
    }
  }

  public class ResumptionItemStatusModel {
    private ResumptionItemStatus item;
    private SimpleBigDecimalProperty countProperty = new SimpleBigDecimalProperty();

    public ResumptionItemStatusModel(ResumptionItemStatus item) {
      this.item = item;
    }

    public ResumptionItemStatus getItem() {
      return item;
    }

    public void setItem(ResumptionItemStatus item) {
      this.item = item;
    }


    public SimpleBigDecimalProperty countProperty() {
      return countProperty;
    }

    public void setCount(BigDecimal count) {
      this.countProperty.set(count);
    }
  }
}
