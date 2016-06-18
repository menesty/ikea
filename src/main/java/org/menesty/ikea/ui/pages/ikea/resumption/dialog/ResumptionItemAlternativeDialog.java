package org.menesty.ikea.ui.pages.ikea.resumption.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.ResumptionItemStatus;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ClipboardUtil;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 2/26/16.o
 * 13:33.
 */
public class ResumptionItemAlternativeDialog extends BaseDialog {
  private LoadService loadService;
  private TableView<ResumptionItemStatus> tableView;

  public ResumptionItemAlternativeDialog(Stage stage) {
    super(stage);
    setTitle(I18n.UA.getString(I18nKeys.RESUMPTION_ALTERNATIVES));
    setMaxWidth(700);
    tableView = new TableView<>();

    {
      TableColumn<ResumptionItemStatus, Number> column = new TableColumn<>();
      column.setMaxWidth(45);
      column.setCellValueFactory(ColumnUtil.<ResumptionItemStatus>indexColumn());
      tableView.getColumns().add(column);
    }

    {
      TableColumn<ResumptionItemStatus, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setMinWidth(130);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ResumptionItemStatus, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));

      column.setCellValueFactory(ColumnUtil.number("available"));
      column.setMinWidth(60);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ResumptionItemStatus, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.INVOICE_NAME));
      column.setPrefWidth(130);
      column.setCellValueFactory(ColumnUtil.column("invoice.invoiceName"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ResumptionItemStatus, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PARAGON_NUMBER));
      column.setCellValueFactory(ColumnUtil.column("invoice.paragonNumber"));
      column.setPrefWidth(100);
      tableView.getColumns().add(column);
    }

    {
      TableColumn<ResumptionItemStatus, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SELL_DATE));
      column.setCellValueFactory(ColumnUtil.dateColumn("invoice.sellDate"));
      column.setPrefWidth(140);
      tableView.getColumns().add(column);
    }

    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createCopyIcon32());
      button.setOnAction(event -> {
        String copyString = tableView.getItems().stream().map(resumptionItemStatus -> {
          SimpleDateFormat sdf = new SimpleDateFormat(ColumnUtil.DEFAULT_DATE_FORMAT);

          return resumptionItemStatus.getArtNumber() + "\t" + resumptionItemStatus.getAvailable() + "\t" +
              resumptionItemStatus.getInvoice().getInvoiceName() + "\t" + resumptionItemStatus.getInvoice().getParagonNumber() +
              "\t" + sdf.format(resumptionItemStatus.getInvoice().getSellDate());
        }).collect(Collectors.joining("\r\n"));

        ClipboardUtil.copy(copyString);
      });

      toolBar.getItems().add(button);
    }

    BorderPane main = new BorderPane();

    main.setCenter(tableView);
    main.setTop(toolBar);

    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> tableView.getItems().setAll(value));
    loadingPane.bindTask(loadService);

    addRow(main, bottomBar);
  }

  public void setProductId(Long productId) {
    loadService.productIdProperty.setValue(productId);
  }

  @Override
  public void onShow() {
    loadService.restart();
  }

  class LoadService extends AbstractAsyncService<List<ResumptionItemStatus>> {
    private LongProperty productIdProperty = new SimpleLongProperty();

    @Override
    protected Task<List<ResumptionItemStatus>> createTask() {
      final Long _productId = productIdProperty.get();

      return new Task<List<ResumptionItemStatus>>() {
        @Override
        protected List<ResumptionItemStatus> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/resumption/product/" + _productId + "/variants");

          return request.getList(new TypeReference<List<ResumptionItemStatus>>() {
          });
        }
      };
    }
  }
}
