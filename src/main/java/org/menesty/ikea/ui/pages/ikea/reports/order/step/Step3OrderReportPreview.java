package org.menesty.ikea.ui.pages.ikea.reports.order.step;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.parse.RawItem;
import org.menesty.ikea.lib.domain.report.SummaryOrderReport;
import org.menesty.ikea.lib.domain.report.SummaryOrderReportItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.service.parser.FileParseStatistic;
import org.menesty.ikea.service.parser.ParseResult;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;
import org.menesty.ikea.ui.pages.ikea.reports.order.OrderReportInfo;
import org.menesty.ikea.ui.pages.wizard.order.step.service.AbstractExportAsyncService;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 5/26/16.
 * 12:01.
 */
public class Step3OrderReportPreview extends BaseWizardStep<OrderReportInfo> {
  private LoadReportService loadReportService;
  private TableView<SummaryOrderReportItem> reportItemTableView;
  private LoadingPane loadingPane = new LoadingPane();
  private AbstractExportAsyncService<SummaryOrderReport> xlsResultExportAsyncService;
  private SummaryOrderReport summaryOrderReport;

  public Step3OrderReportPreview(DialogSupport dialogSupport) {
    loadingPane = new LoadingPane();

    StackPane mainPane = new StackPane();
    VBox workspace = new VBox();

    mainPane.getChildren().addAll(workspace, loadingPane);

    workspace.setSpacing(4);

    reportItemTableView = new TableView<>();

    {
      TableColumn<SummaryOrderReportItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setCellValueFactory(ColumnUtil.<SummaryOrderReportItem, String>column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);
      column.setMinWidth(130);

      reportItemTableView.getColumns().add(column);
    }

    {
      TableColumn<SummaryOrderReportItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));
      column.setCellValueFactory(ColumnUtil.<SummaryOrderReportItem>number("orderCount"));
      column.setMinWidth(120);

      reportItemTableView.getColumns().add(column);
    }

    {
      TableColumn<SummaryOrderReportItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.INVOICE_ITEM_COUNT));
      column.setCellValueFactory(ColumnUtil.<SummaryOrderReportItem>number("invoiceCount"));
      column.setMinWidth(120);

      reportItemTableView.getColumns().add(column);
    }

    {
      TableColumn<SummaryOrderReportItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.INVOICE_PRICE));
      column.setCellValueFactory(ColumnUtil.<SummaryOrderReportItem>number("invoicePrice"));
      column.setMinWidth(120);

      reportItemTableView.getColumns().add(column);
    }

    {
      TableColumn<SummaryOrderReportItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ACCEPTED_COUNT));
      column.setCellValueFactory(ColumnUtil.<SummaryOrderReportItem>number("acceptedCount"));
      column.setMinWidth(120);

      reportItemTableView.getColumns().add(column);
    }

    {
      TableColumn<SummaryOrderReportItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ACCEPTED_PRICE));
      column.setCellValueFactory(ColumnUtil.<SummaryOrderReportItem>number("acceptedPrice"));
      column.setMinWidth(120);

      reportItemTableView.getColumns().add(column);
    }

    BorderPane borderPane = new BorderPane();

    borderPane.setCenter(reportItemTableView);

    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createXlsExport32Icon());
      button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.XLS_EXPORT)));
      button.setOnAction(event -> {
        File selectedFile = FileChooserUtil.getXls().showSaveDialog(dialogSupport.getStage());

        if (selectedFile != null) {
          xlsResultExportAsyncService.setFile(selectedFile);
          xlsResultExportAsyncService.setParam(summaryOrderReport);
          xlsResultExportAsyncService.restart();
        }
      });

      toolBar.getItems().add(button);
    }

    borderPane.setTop(toolBar);

    workspace.getChildren().add(borderPane);

    setContent(mainPane);

    xlsResultExportAsyncService = new AbstractExportAsyncService<SummaryOrderReport>() {
      @Override
      protected void export(File file, SummaryOrderReport param) {
        ServiceFacade.getXlsExportService().exportSummaryOrderReport(file, param);
      }
    };
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public boolean canSkip(OrderReportInfo param) {
    return false;
  }

  @Override
  public void collect(OrderReportInfo param) {

  }

  @Override
  public void onActive(OrderReportInfo param) {
    List<RawItem> items = reduce(param.getFileParseStatistics());

    stopAsyncServices();
    getWizardPanel().lockButtons();

    summaryOrderReport = null;
    loadReportService = new LoadReportService();
    loadReportService.setOnSucceededListener(value -> {
      getWizardPanel().unLockButtons();
      summaryOrderReport = value;
      reportItemTableView.getItems().setAll(value.getReportItemList());
    });

    loadReportService.setDate(param.getIkeaProcessOrder().getId(), items);
    loadingPane.bindTask(loadReportService);
    loadReportService.restart();
  }


  private List<RawItem> reduce(List<FileParseStatistic> fileParseStatistics) {

    Map<String, List<RawItem>> itemsMap = fileParseStatistics.stream()
        .map(FileParseStatistic::getParseResult)
        .map(ParseResult::getRawOrderItems)
        .flatMap(Collection::stream)
        .collect(Collectors.groupingBy(RawItem::getArtNumber));

    List<RawItem> items = new ArrayList<>();

    itemsMap.values().stream().forEach(rawItems -> {
      RawItem item = rawItems.get(0);

      if (rawItems.size() > 1) {
        item.setCount(rawItems.stream().map(RawItem::getCount).reduce(BigDecimal.ZERO, BigDecimal::add));
      }

      items.add(item);
    });

    return items;
  }


  class LoadReportService extends AbstractAsyncService<SummaryOrderReport> {
    private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();
    private ObjectProperty<List<RawItem>> rawItemProperty = new SimpleObjectProperty<>();

    @Override
    protected Task<SummaryOrderReport> createTask() {
      final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();
      final List<RawItem> _rawItems = rawItemProperty.get();

      return new Task<SummaryOrderReport>() {
        @Override
        protected SummaryOrderReport call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/reports/summary/order/" + _ikeaProcessOrderId);

          return request.postData(_rawItems, SummaryOrderReport.class);
        }
      };
    }

    public void setDate(Long ikeaProcessOrderId, List<RawItem> rawItems) {
      ikeaProcessOrderIdProperty.set(ikeaProcessOrderId);
      rawItemProperty.setValue(rawItems);
    }
  }


  private void stopAsyncServices() {
    if (loadReportService != null) {
      loadReportService.cancel();
    }
  }
}
