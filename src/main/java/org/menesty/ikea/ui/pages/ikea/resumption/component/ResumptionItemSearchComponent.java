package org.menesty.ikea.ui.pages.ikea.resumption.component;

import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.Resumption;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.ResumptionItem;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.table.EntityCheckBoxHolder;
import org.menesty.ikea.ui.pages.ikea.resumption.service.ResumptionItemLoadService;
import org.menesty.ikea.ui.pages.ikea.resumption.service.XlsResumptionItemExportService;
import org.menesty.ikea.util.DateUtil;
import org.menesty.ikea.util.FileChooserUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 3/9/16.
 * 12:57.
 */
public class ResumptionItemSearchComponent extends StackPane {
  private final ListView<EntityCheckBoxHolder<Resumption>> listView;
  private final ResumptionItemTableView resumptionItemTableView;
  private final ResumptionItemLoadService loadService;
  private List<ResumptionItem> resumptionItems = new ArrayList<>();
  private XlsResumptionItemExportService xlsDataExportService;

  public ResumptionItemSearchComponent(final DialogSupport dialogSupport) {
    xlsDataExportService = new XlsResumptionItemExportService();
    listView = new ListView<>();
    listView.setCellFactory(
        CheckBoxListCell.forListView(
            EntityCheckBoxHolder::checkedProperty, new StringConverter<EntityCheckBoxHolder<Resumption>>() {
              @Override
              public String toString(EntityCheckBoxHolder<Resumption> object) {
                return object != null ? DateUtil.format(object.getItem().getCreatedDate()) + " (" + object.getItem().getId() + ")" : "";
              }

              @Override
              public EntityCheckBoxHolder<Resumption> fromString(String string) {
                return listView.getItems().stream().filter(resumptionEntityCheckBoxHolder -> {
                  String date = DateUtil.format(resumptionEntityCheckBoxHolder.getItem().getCreatedDate());
                  return string.equals(date + " (" + resumptionEntityCheckBoxHolder.getItem().getId() + ")");
                }).findFirst().orElse(null);
              }
            }
        )
    );

    listView.setMaxWidth(250);
    BorderPane borderPane = new BorderPane();
    borderPane.setCenter(resumptionItemTableView = new ResumptionItemTableView(dialogSupport));

    VBox toolBarPane = new VBox();

    ToolBar toolBar = new ToolBar();

    {
      TextField artNumber = new TextField();
      artNumber.setDelay(1);
      artNumber.setOnDelayAction(actionEvent -> applyFilter(artNumber.getText()));
      artNumber.setPromptText("Product ID #");

      toolBar.getItems().add(artNumber);
    }

    ToolBar actionToolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createXlsExport32Icon());
      button.setOnAction(event -> {
        FileChooser fileChooser = FileChooserUtil.getXls();
        File selectedFile = fileChooser.showSaveDialog(dialogSupport.getStage());

        if (selectedFile != null) {
          xlsDataExportService.setData(selectedFile, resumptionItemTableView.getItems());
          xlsDataExportService.restart();
        }
      });

      actionToolBar.getItems().add(button);
    }

    toolBarPane.getChildren().addAll(actionToolBar, toolBar);
    borderPane.setTop(toolBarPane);

    LoadingPane loadingPane = new LoadingPane();

    HBox mainPane = new HBox();

    HBox.setHgrow(borderPane, Priority.ALWAYS);
    mainPane.getChildren().add(listView);
    mainPane.getChildren().add(borderPane);

    getChildren().addAll(mainPane, loadingPane);

    loadService = new ResumptionItemLoadService();
    loadService.setOnSucceededListener(value -> {
      resumptionItems.addAll(value);
      resumptionItemTableView.getItems().setAll(resumptionItems);
    });

    loadingPane.bindTask(loadService);
  }

  private void applyFilter(String artNumber) {
    List<ResumptionItem> items;
    if (StringUtils.isNotBlank(artNumber)) {
      items = resumptionItems.stream()
          .filter(entity -> entity.getArtNumber().contains(artNumber))
          .collect(Collectors.toList());
    } else {
      items = resumptionItems;
    }

    resumptionItemTableView.getItems().setAll(items);
  }

  public void setResumptions(List<Resumption> resumptions) {
    listView.getItems().setAll(resumptions.stream().map(EntityCheckBoxHolder<Resumption>::new).collect(Collectors.toList()));
    listView.getItems().stream().forEach(resumptionEntityCheckBoxHolder -> {
      resumptionEntityCheckBoxHolder.checkedProperty().addListener((observable, oldValue, newValue) -> {
        Resumption resumption = resumptionEntityCheckBoxHolder.getItem();
        if (newValue) {
          loadService.setResumptionId(resumption.getId());
          loadService.restart();
        } else {
          resumptionItems = resumptionItems.stream()
              .filter(resumptionItem -> !resumptionItem.getResumptionId().equals(resumption.getId()))
              .collect(Collectors.toList());
          resumptionItemTableView.getItems().setAll(resumptionItems);
        }
      });
    });
  }

}
