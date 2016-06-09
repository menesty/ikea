package org.menesty.ikea.ui.pages.ikea.reports.order.step;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.parse.RawItem;
import org.menesty.ikea.service.parser.FileParseStatistic;
import org.menesty.ikea.service.parser.ParseResult;
import org.menesty.ikea.service.parser.PdfParserOrder;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;
import org.menesty.ikea.ui.pages.ikea.reports.order.OrderReportInfo;
import org.menesty.ikea.ui.pages.wizard.order.step.component.ItemProcessingInfoLabel;
import org.menesty.ikea.ui.pages.wizard.order.step.service.AbstractParseOrderService;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 9/7/15.
 * 17:42.
 */
public class Step2ParseDocuments extends BaseWizardStep<OrderReportInfo> {
  private ItemProcessingInfoLabel fileProcessingInfoLabel;
  private TableView<FileParseStatistic> processedFileTableView;
  private TableView<RawItem> rawItemTableView;
  private AbstractParseOrderService parseAbstractAsyncService;

  public Step2ParseDocuments() {
    VBox mainPane = new VBox();
    mainPane.setSpacing(4);

    fileProcessingInfoLabel = new ItemProcessingInfoLabel(I18n.UA.getString(I18nKeys.PROCESSING_FILE));

    processedFileTableView = new TableView<>();

    {
      TableColumn<FileParseStatistic, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.FILE_NAME));
      column.setCellValueFactory(ColumnUtil.<FileParseStatistic, String>column("fileName"));
      column.setMinWidth(200);
      processedFileTableView.getColumns().add(column);

    }
    {
      TableColumn<FileParseStatistic, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PARSED_ITEM_ROWS));
      column.setMinWidth(200);
      column.setCellValueFactory(ColumnUtil.<FileParseStatistic, Number>column("itemCount"));
      processedFileTableView.getColumns().add(column);
    }

    {
      TableColumn<FileParseStatistic, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PARSED_ITEM_ERRORS));
      column.setCellValueFactory(ColumnUtil.<FileParseStatistic, Number>column("warningCount"));
      column.setMinWidth(140);
      processedFileTableView.getColumns().add(column);
    }

    {
      TableColumn<FileParseStatistic, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ITEM_SUM_PRICE));
      column.setCellValueFactory(ColumnUtil.<FileParseStatistic, Number>column("sumFormat"));
      column.setMinWidth(140);
      processedFileTableView.getColumns().add(column);
    }

    {
      TableColumn<FileParseStatistic, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE_PDF));
      column.setCellValueFactory(ColumnUtil.<FileParseStatistic, Number>column("parseResultSumFormat"));
      column.setMinWidth(140);
      processedFileTableView.getColumns().add(column);
    }

    processedFileTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      rawItemTableView.getItems().clear();

      if (newValue != null) {
        rawItemTableView.getItems().addAll(newValue.getParseResult().getRawOrderItems());
      }
    });

    rawItemTableView = new TableView<>();

    {
      TableColumn<RawItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setCellFactory(ArtNumberCell::new);
      column.setCellValueFactory(ColumnUtil.<RawItem, String>column("artNumber"));
      column.setMinWidth(130);
      rawItemTableView.getColumns().add(column);
    }
    {
      TableColumn<RawItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));
      column.setCellValueFactory(ColumnUtil.<RawItem>number("count"));
      column.setMinWidth(60);
      rawItemTableView.getColumns().add(column);
    }

    {
      TableColumn<RawItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
      column.setCellValueFactory(ColumnUtil.<RawItem>number("price"));
      column.setMinWidth(80);
      rawItemTableView.getColumns().add(column);
    }


    mainPane.getChildren().addAll(fileProcessingInfoLabel, processedFileTableView, rawItemTableView);
    setContent(mainPane);
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
    param.setFileParseStatistics(processedFileTableView.getItems());
  }

  @Override
  public void onActive(OrderReportInfo param) {
    stopAsyncServices();
    processedFileTableView.getItems().clear();
    rawItemTableView.getItems().clear();

    startProcessFiles(param.getFiles());
  }

  private void stopAsyncServices() {
    if (parseAbstractAsyncService != null) {
      parseAbstractAsyncService.cancel();
    }
  }

  private void startProcessFiles(List<File> files) {
    getWizardPanel().lockButtons();
    fileProcessingInfoLabel.setTotal(files.size());


    parseAbstractAsyncService = new PdfParseService();

    parseAbstractAsyncService.setFiles(files);

    fileProcessingInfoLabel.setIndexProperty(parseAbstractAsyncService.fileIndexProperty());
    fileProcessingInfoLabel.setNameProperty(parseAbstractAsyncService.fileNameProperty());

    parseAbstractAsyncService.setOnSucceededListener(value -> {
      fileProcessingInfoLabel.hideProgress();
      getWizardPanel().unLockButtons();
      List<FileParseStatistic> statistics = value.stream().map(this::statistic).collect(Collectors.toList());

      processedFileTableView.getItems().addAll(statistics);
    });

    fileProcessingInfoLabel.showProgress();
    parseAbstractAsyncService.restart();
  }


  class PdfParseService extends AbstractParseOrderService {
    private PdfParserOrder pdfParserOrder;

    public PdfParseService() {
      pdfParserOrder = new PdfParserOrder();
    }

    @Override
    protected ParseResult parse(File file) throws FileNotFoundException {
      return pdfParserOrder.parse(new FileInputStream(file));
    }
  }

  private FileParseStatistic statistic(ParseResult parseResult) {

    FileParseStatistic fileParseStatistic = new FileParseStatistic(parseResult);

    BigDecimal sum = parseResult.getRawOrderItems()
        .stream()
        .map(RawItem::getTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    fileParseStatistic.setSum(sum);

    return fileParseStatistic;
  }
}

