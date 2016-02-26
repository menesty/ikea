package org.menesty.ikea.ui.pages.ikea.resumption;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.Resumption;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.ResumptionItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.xls.XlsExportService;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.FileChooserUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.io.File;
import java.util.List;

/**
 * Created by Menesty on
 * 2/25/16.
 * 14:11.
 */
public class ResumptionDetailPage extends BasePage {
  private BaseTableView<ResumptionItem> tableView;

  private LoadService loadService;
  private XlsDataExportService xlsDataExportService;

  @Override
  protected void initialize() {
    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> tableView.getItems().setAll(value));
    xlsDataExportService = new XlsDataExportService();
  }

  @Override
  protected Node createView() {
    BorderPane main = new BorderPane();

    tableView = new BaseTableView<>();

    {
      TableColumn<ResumptionItem, Number> column = new TableColumn<>();
      column.setMaxWidth(45);
      column.setCellValueFactory(ColumnUtil.<ResumptionItem>indexColumn());
      tableView.getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setMinWidth(150);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));

      column.setCellValueFactory(ColumnUtil.number("count"));
      column.setMinWidth(60);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.INVOICE_NAME));
      column.setPrefWidth(150);
      column.setCellValueFactory(ColumnUtil.column("invoice.invoiceName"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PARAGON_NUMBER));
      column.setCellValueFactory(ColumnUtil.column("invoice.paragonNumber"));
      column.setPrefWidth(150);
      tableView.getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SELL_DATE));
      column.setCellValueFactory(ColumnUtil.dateColumn("invoice.sellDate"));
      column.setPrefWidth(150);
      tableView.getColumns().add(column);
    }

    main.setCenter(tableView);

    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createXlsExport32Icon());
      button.setOnAction(event -> {
        FileChooser fileChooser = FileChooserUtil.getXls();
        File selectedFile = fileChooser.showSaveDialog(getDialogSupport().getStage());

        if (selectedFile != null) {
          xlsDataExportService.setData(selectedFile, tableView.getItems());
          xlsDataExportService.restart();
        }
      });

      toolBar.getItems().add(button);
    }

    main.setTop(toolBar);

    return wrap(main);
  }

  @Override
  public void onActive(Object... params) {
    Resumption resumption = (Resumption) params[0];

    loadingPane.bindTask(loadService, xlsDataExportService);

    loadService.setResumptionId(resumption.getId());
    loadService.restart();
  }

  class LoadService extends AbstractAsyncService<List<ResumptionItem>> {
    private LongProperty resumptionIdProperty = new SimpleLongProperty();

    @Override
    protected Task<List<ResumptionItem>> createTask() {
      final Long _resumptionId = resumptionIdProperty.get();

      return new Task<List<ResumptionItem>>() {
        @Override
        protected List<ResumptionItem> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/resumption/" + _resumptionId + "/items");

          return request.getData(new TypeReference<List<ResumptionItem>>() {
          });
        }
      };
    }

    public void setResumptionId(Long resumptionId) {
      resumptionIdProperty.setValue(resumptionId);
    }
  }

  class XlsDataExportService extends AbstractAsyncService<Void> {
    private ObjectProperty<List<ResumptionItem>> resumptionItemListProperty = new SimpleObjectProperty<>();
    private ObjectProperty<File> targetFileProperty = new SimpleObjectProperty<>();

    @Override
    protected Task<Void> createTask() {
      final List<ResumptionItem> _resumptionItems = resumptionItemListProperty.get();
      final File _targetFile = targetFileProperty.get();

      return new Task<Void>() {
        @Override
        protected Void call() throws Exception {
          XlsExportService xlsExportService = new XlsExportService();

          xlsExportService.exportResumptionItems(_targetFile, _resumptionItems);
          return null;
        }
      };
    }

    public void setData(File targetFile, List<ResumptionItem> resumptionItems) {
      resumptionItemListProperty.setValue(resumptionItems);
      targetFileProperty.setValue(targetFile);
    }
  }
}
