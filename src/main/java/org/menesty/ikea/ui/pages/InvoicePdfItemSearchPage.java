package org.menesty.ikea.ui.pages;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.service.SucceededListener;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.util.ClipboardUtil;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.DateFormatter;
import org.menesty.ikea.util.NumberUtil;

import java.text.SimpleDateFormat;
import java.util.List;

public class InvoicePdfItemSearchPage extends BasePage {
  private TextField artNumber;
  private LoadService loadService;
  private BaseTableView<RawInvoiceProductItem> tableView;

  public InvoicePdfItemSearchPage() {
    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> tableView.getItems().setAll(value));
  }

  public void onActive(Object... params) {
    loadingPane.bindTask(loadService);
  }

  @Override
  protected Node createView() {
    ToolBar toolBar = new ToolBar();

    artNumber = new TextField();
    artNumber.setDelay(1);
    artNumber.setOnDelayAction(actionEvent -> {
      loadService.setArtNumber(artNumber.getText());
      loadService.restart();
    });
    artNumber.setPromptText("Product ID #");

    toolBar.getItems().add(artNumber);

    tableView = new BaseTableView<>();
    {
      TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>("Art # ");
      column.setMinWidth(100);
      column.setCellValueFactory(ColumnUtil.<RawInvoiceProductItem, String>column("prepareArtNumber"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>("Invoice #");
      column.setMinWidth(100);
      column.setCellValueFactory(ColumnUtil.<RawInvoiceProductItem, String>column("invoicePdf.invoiceNumber"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>("Paragon #");
      column.setMinWidth(100);
      column.setCellValueFactory(ColumnUtil.<RawInvoiceProductItem, String>column("invoicePdf.paragonName"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>("Name");
      column.setMinWidth(200);
      column.setCellValueFactory(ColumnUtil.<RawInvoiceProductItem, String>column("name"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>("Count");
      column.setMaxWidth(50);
      column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RawInvoiceProductItem, String>, ObservableValue<String>>() {
        @Override
        public ObservableValue<String> call(TableColumn.CellDataFeatures<RawInvoiceProductItem, String> item) {
          return new SimpleStringProperty(NumberUtil.toString(item.getValue().getCount()));
        }
      });

      tableView.getColumns().add(column);
    }


    {
      TableColumn<RawInvoiceProductItem, Double> column = new TableColumn<>("Price");
      column.setMinWidth(60);
      column.setCellValueFactory(ColumnUtil.<RawInvoiceProductItem, Double>column("price"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<RawInvoiceProductItem, Double> column = new TableColumn<>("T Price");
      column.setMinWidth(60);
      column.setCellValueFactory(ColumnUtil.<RawInvoiceProductItem, Double>column("total"));

      tableView.getColumns().add(column);
    }

    tableView.setRowRenderListener((row, newValue) -> {
      row.setContextMenu(null);

      if (newValue != null) {
        ContextMenu contextMenu = new ContextMenu();

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.COPY));
          menuItem.setOnAction(event -> {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(ColumnUtil.DEFAULT_DATE_FORMAT);
            String copyString = newValue.getOriginalArtNumber() + "\t" + newValue.getCount() + "\t" + newValue.getPrice() +
                "\t" + newValue.getInvoicePdf().getInvoiceNumber() + "\t" + newValue.getInvoicePdf().getParagonName() +
                "\t" + dateFormatter.format(newValue.getInvoicePdf().getParagonDate());
            ClipboardUtil.copy(copyString);
          });

          contextMenu.getItems().add(menuItem);
        }

        row.setContextMenu(contextMenu);
      }
    });


    BorderPane main = new BorderPane();
    main.setTop(toolBar);
    main.setCenter(tableView);

    return wrap(main);
  }

  class LoadService extends AbstractAsyncService<List<RawInvoiceProductItem>> {
    private SimpleStringProperty artNumberProperty = new SimpleStringProperty();

    @Override
    protected Task<List<RawInvoiceProductItem>> createTask() {
      final String artNumber = artNumberProperty.getValue();

      return new Task<List<RawInvoiceProductItem>>() {
        @Override
        protected List<RawInvoiceProductItem> call() throws Exception {
          return ServiceFacade.getInvoicePdfService().searchItemsByArtNumber(artNumber);
        }
      };
    }

    public void setArtNumber(String artNumber) {
      artNumberProperty.set(artNumber);
    }
  }
}
