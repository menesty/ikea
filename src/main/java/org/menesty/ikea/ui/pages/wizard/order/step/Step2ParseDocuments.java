package org.menesty.ikea.ui.pages.wizard.order.step;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.parse.RawItem;
import org.menesty.ikea.lib.dto.DesktopOrderInfo;
import org.menesty.ikea.lib.dto.OrderItemDetails;
import org.menesty.ikea.service.parser.*;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;
import org.menesty.ikea.ui.pages.wizard.order.step.component.ItemProcessingInfoLabel;
import org.menesty.ikea.ui.pages.wizard.order.step.service.AbstractParseOrderService;
import org.menesty.ikea.ui.pages.wizard.order.step.service.ProductInfoAsyncService;
import org.menesty.ikea.util.ColumnUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 9/7/15.
 * 17:42.
 */
public class Step2ParseDocuments extends BaseWizardStep<DesktopOrderInfo> {
  private final ItemProcessingInfoLabel productProcessingInfoLabel;
  private ItemProcessingInfoLabel fileProcessingInfoLabel;
  private TableView<FileParseStatistic> processedFileTableView;
  private TableView<ErrorMessage> fileErrorMessagesTableView;
  private AbstractParseOrderService parseAbstractAsyncService;
  private OrderItemDetails orderItemDetails;
  private ProductInfoAsyncService productInfoAsyncService;

  public Step2ParseDocuments() {
    VBox mainPane = new VBox();
    mainPane.setSpacing(4);

    fileProcessingInfoLabel = new ItemProcessingInfoLabel(I18n.UA.getString(I18nKeys.PROCESSING_FILE));
    productProcessingInfoLabel = new ItemProcessingInfoLabel(I18n.UA.getString(I18nKeys.PROCESSING_PRODUCT));

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
      TableColumn<FileParseStatistic, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE_SUM));
      column.setCellValueFactory(ColumnUtil.<FileParseStatistic, Number>column("sumFormat"));
      column.setMinWidth(140);
      processedFileTableView.getColumns().add(column);
    }

    processedFileTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      fileErrorMessagesTableView.getItems().clear();

      if (newValue != null) {
        fileErrorMessagesTableView.getItems().addAll(newValue.getParseResult().getParseWarnings());
      }
    });

    fileErrorMessagesTableView = new TableView<>();

    {
      TableColumn<ErrorMessage, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ERROR));
      column.setCellValueFactory(ColumnUtil.<ErrorMessage, String>column("message"));
      column.setMinWidth(300);
      fileErrorMessagesTableView.getColumns().add(column);
    }

    {
      TableColumn<ErrorMessage, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.DETAILS));
      column.setCellValueFactory(ColumnUtil.<ErrorMessage, String>column("exception"));
      column.setMinWidth(400);
      fileErrorMessagesTableView.getColumns().add(column);
    }


    mainPane.getChildren().addAll(fileProcessingInfoLabel, productProcessingInfoLabel, processedFileTableView, fileErrorMessagesTableView);
    setContent(mainPane);
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public boolean canSkip(DesktopOrderInfo param) {
    return DesktopOrderInfo.SourceType.MANUAL == param.getSourceType() || DesktopOrderInfo.SourceType.SITE == param.getSourceType();
  }

  @Override
  public void collect(DesktopOrderInfo param) {
    param.setOrderItemDetails(orderItemDetails);
  }

  @Override
  public void onActive(DesktopOrderInfo param) {
    stopAsyncServices();
    processedFileTableView.getItems().clear();
    fileErrorMessagesTableView.getItems().clear();

    DesktopOrderInfo.SourceType sourceType = param.getSourceType();

    if (sourceType == null || DesktopOrderInfo.SourceType.MANUAL == sourceType || DesktopOrderInfo.SourceType.SITE == param.getSourceType()) {
      return;
    }

    startProcessFiles(param.getSourceType(), param.getFiles());
  }

  private void stopAsyncServices() {
    if (productInfoAsyncService != null) {
      productInfoAsyncService.cancel();
    }

    if (parseAbstractAsyncService != null) {
      parseAbstractAsyncService.cancel();
    }
  }

  private void startProcessFiles(DesktopOrderInfo.SourceType sourceType, List<File> files) {
    getWizardPanel().lockButtons();
    fileProcessingInfoLabel.setTotal(files.size());

    if (DesktopOrderInfo.SourceType.XLS == sourceType) {
      parseAbstractAsyncService = new XlsParseService();
    } else if (DesktopOrderInfo.SourceType.PDF == sourceType) {
      parseAbstractAsyncService = new PdfParseService();
    }

    parseAbstractAsyncService.setFiles(files);

    fileProcessingInfoLabel.setIndexProperty(parseAbstractAsyncService.fileIndexProperty());
    fileProcessingInfoLabel.setNameProperty(parseAbstractAsyncService.fileNameProperty());

    parseAbstractAsyncService.setOnSucceededListener(value -> {
      fileProcessingInfoLabel.hideProgress();

      List<FileParseStatistic> statistics = value.stream().map(this::statistic).collect(Collectors.toList());

      processedFileTableView.getItems().addAll(statistics);
      syncProductsWithIkea(value);
    });

    fileProcessingInfoLabel.showProgress();
    parseAbstractAsyncService.restart();
  }

  private void syncProductsWithIkea(List<ParseResult> parseResults) {
    List<RawItem> rawItems = reduce(parseResults);

    productInfoAsyncService = new ProductInfoAsyncService();
    productInfoAsyncService.setOnSucceededListener(value -> {
      productProcessingInfoLabel.hideProgress();
      orderItemDetails = value;

      getWizardPanel().unLockButtons();
    });

    productInfoAsyncService.setRawItems(rawItems);
    productProcessingInfoLabel.setTotal(rawItems.size());
    productProcessingInfoLabel.setIndexProperty(productInfoAsyncService.artNumberIndexProperty());
    productProcessingInfoLabel.setNameProperty(productInfoAsyncService.artNumberProperty());
    productProcessingInfoLabel.showProgress();
    productInfoAsyncService.restart();
  }

  private List<RawItem> reduce(List<ParseResult> parseResults) {
    Map<String, RawItem> reduceMap = new HashMap<>();

    parseResults.stream().forEach(parseResult -> {
      parseResult.getRawOrderItems().stream().forEach(rawItem -> {
        RawItem current = reduceMap.get(rawItem.getArtNumber());

        if (current == null || StringUtils.isNotBlank(rawItem.getComment())) {
          String keyPrefix = StringUtils.isNotBlank(rawItem.getComment()) ? unique() : "";
          reduceMap.put(rawItem.getArtNumber() + keyPrefix, rawItem);
        } else {
          current.setCount(current.getCount().add(rawItem.getCount()));
        }
      });
    });

    return new ArrayList<>(reduceMap.values());
  }

  private String unique() {
    return RandomStringUtils.random(6, true, true);
  }

  class XlsParseService extends AbstractParseOrderService {
    private InputStream configuration;
    private XlsParserOrder xlsParserOrder;

    public XlsParseService() {
      configuration = Step2ParseDocuments.class.getResourceAsStream("/config/raw_config.xml");
      this.xlsParserOrder = new XlsParserOrder();
    }

    @Override
    protected ParseResult parse(File file) throws FileNotFoundException {
      return xlsParserOrder.parse(new FileInputStream(file), configuration);
    }
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

