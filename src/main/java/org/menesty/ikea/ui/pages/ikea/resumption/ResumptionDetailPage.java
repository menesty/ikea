package org.menesty.ikea.ui.pages.ikea.resumption;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.Resumption;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.ResumptionItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.xls.XlsExportService;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.ikea.resumption.component.ResumptionItemTableView;
import org.menesty.ikea.ui.pages.ikea.resumption.service.ResumptionItemLoadService;
import org.menesty.ikea.ui.pages.ikea.resumption.service.XlsResumptionItemExportService;
import org.menesty.ikea.util.FileChooserUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 2/25/16.
 * 14:11.
 */
public class ResumptionDetailPage extends BasePage {
  private ResumptionItemTableView tableView;

  private ResumptionItemLoadService loadService;
  private XlsResumptionItemExportService xlsDataExportService;
  private TotalStatusPanel totalStatusPanel;

  private List<ResumptionItem> resumptionItems;

  @Override
  protected void initialize() {
    loadService = new ResumptionItemLoadService();
    loadService.setOnSucceededListener(value -> {
      setItems(value);
      resumptionItems = value;
    });
    xlsDataExportService = new XlsResumptionItemExportService();
  }

  @Override
  protected Node createView() {
    BorderPane main = new BorderPane();

    main.setCenter(tableView = new ResumptionItemTableView(getDialogSupport()));

    ToolBar toolBar = new ToolBar();

    {
      TextField artNumber = new TextField();
      artNumber.setDelay(1);
      artNumber.setOnDelayAction(actionEvent -> applyFilter(artNumber.getText()));
      artNumber.setPromptText("Product ID #");

      toolBar.getItems().add(artNumber);
    }

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
    main.setBottom(totalStatusPanel = new TotalStatusPanel());

    return wrap(main);
  }

  private void applyFilter(String artNumber) {
    List<ResumptionItem> items;
    if (StringUtils.isNotBlank(artNumber)) {
      items = resumptionItems.stream()
          .filter(resumptionItem -> resumptionItem.getArtNumber().contains(artNumber))
          .collect(Collectors.toList());
    } else {
      items = resumptionItems;
    }
    setItems(items);
  }

  private void setItems(List<ResumptionItem> items) {
    tableView.getItems().setAll(items);

    BigDecimal total = items.stream()
        .filter(resumptionItem -> resumptionItem.getInvoiceItem() != null)
        .map(resumptionItem -> resumptionItem.getInvoiceItem().getPrice().multiply(resumptionItem.getCount()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    totalStatusPanel.setTotal(total);
  }

  @Override
  public void onActive(Object... params) {
    Resumption resumption = (Resumption) params[0];

    loadingPane.bindTask(loadService, xlsDataExportService);

    loadService.setResumptionId(resumption.getId());
    loadService.restart();
  }



}
