package org.menesty.ikea.ui.pages.ikea.warehouse;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.EppInformation;
import org.menesty.ikea.lib.domain.ikea.logistic.paragon.Paragon;
import org.menesty.ikea.lib.dto.PageResult;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.ikea.warehouse.dialog.ParagonViewDialog;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.FileChooserUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;

public class ParagonViewComponent extends BorderPane {
  private final LoadService loadService;

  private BaseTableView<Paragon> tableView;

  private ParagonEppService paragonEppService;

  private ParagonCancelService paragonCancelService;

  private ParagonViewDialog paragonViewDialog;

  private static final int ITEM_PER_PAGE = 20;


  private Pagination pagination;

  public ParagonViewComponent(final DialogSupport dialogSupport) {
    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> {
          tableView.getItems().setAll(value.getData());
          pagination.setPageCount(value.getCount() / ITEM_PER_PAGE);
        }
    );

    paragonCancelService = new ParagonCancelService();
    paragonCancelService.setOnSucceededListener(value -> load());

    paragonEppService = new ParagonEppService();
    paragonEppService.setOnSucceededListener(value -> {
    });

    paragonViewDialog = new ParagonViewDialog(dialogSupport.getStage()) {
      @Override
      public void onOk() {
        dialogSupport.hidePopupDialog();
      }
    };

    tableView = new BaseTableView<Paragon>() {
      @Override
      protected void onRowRender(TableRow<Paragon> row, final Paragon paragon) {
        row.getStyleClass().remove("greenRow");
        row.setContextMenu(null);

        if (paragon == null)
          return;

        ContextMenu contextMenu = new ContextMenu();
        {

          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.PARAGON_DETAILS));
          menuItem.setOnAction(actionEvent -> {
            dialogSupport.showPopupDialog(paragonViewDialog);
            paragonViewDialog.show(paragon);
          });

          contextMenu.getItems().add(menuItem);

        }

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.DOWNLOAD_EPP), ImageFactory.createDownload16Icon());
          menuItem.setOnAction(actionEvent -> {
            FileChooser fileChooser = FileChooserUtil.getEpp();
            fileChooser.setInitialFileName("paragon_" + paragon.getId() + ".epp");

            File selectedFile = fileChooser.showSaveDialog(dialogSupport.getStage());

            if (selectedFile != null) {
              paragonEppService.setParagon(selectedFile, paragon);
              paragonEppService.restart();
            }
          });
          contextMenu.getItems().add(menuItem);
        }


        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.PARAGON_CANCEL));
          menuItem.setOnAction(actionEvent -> {
            paragonCancelService.setParagonId(paragon.getId());
            paragonCancelService.restart();
          });

          contextMenu.getItems().add(menuItem);
        }

        row.setContextMenu(contextMenu);

        if (paragon.getDownloadedDate() != null) {
          row.getStyleClass().add("greenRow");
        }
      }
    };

    {
      TableColumn<Paragon, Number> column = new TableColumn<>();
      column.setMaxWidth(45);
      column.setCellValueFactory(ColumnUtil.<Paragon>indexColumn());
      tableView.getColumns().add(column);
    }

    {
      TableColumn<Paragon, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.CREATED_DATE));
      column.setMinWidth(150);
      column.setCellValueFactory(ColumnUtil.dateColumn("createdDate"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<Paragon, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.AMOUNT));
      column.setMinWidth(80);
      column.setCellValueFactory(ColumnUtil.<Paragon>number("amount"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<Paragon, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.DOWNLOAD_DATE));
      column.setMinWidth(150);
      String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
      column.setCellValueFactory(ColumnUtil.dateColumn(DATE_FORMAT, "downloadedDate"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<Paragon, Long> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PARAGON_ID));
      column.setMinWidth(180);
      column.setCellValueFactory(ColumnUtil.<Paragon, Long>column("id"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<Paragon, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.CONTRAGENT));
      column.setMinWidth(180);
      column.setCellValueFactory(param -> {
        SimpleStringProperty contragentProperty = new SimpleStringProperty();

        Paragon paragon = param.getValue();

        if (paragon != null && paragon.getContragent() != null) {
          contragentProperty.set(paragon.getContragent().getFirstName() + " " + paragon.getContragent().getLastName());
        }

        return contragentProperty;
      });
      tableView.getColumns().add(column);
    }

    ToolBar control = new ToolBar();
    Button refresh = new Button(null, ImageFactory.createReload32Icon());
    refresh.setOnAction(actionEvent -> load());
    control.getItems().add(refresh);

    setCenter(tableView);
    setTop(control);

    pagination = new Pagination(1, 0);

    pagination.currentPageIndexProperty().addListener((observable, oldValue, pageIndex) -> {
      loadService.setPageIndex(pageIndex.intValue());
      loadService.restart();
    });

    setBottom(pagination);
  }

  public void load() {
    loadService.setPageIndex(1);
    loadService.restart();
  }

  public Collection<? extends Worker<?>> getWorkers() {
    return Arrays.asList(paragonEppService, paragonCancelService);
  }


  class ParagonEppService extends AbstractAsyncService<Void> {
    private ObjectProperty<Paragon> paragonProperty = new SimpleObjectProperty<>();
    private ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();

    @Override
    protected Task<Void> createTask() {
      final Paragon _paragon = paragonProperty.get();
      final File _file = fileProperty.get();
      return new Task<Void>() {
        @Override
        protected Void call() throws Exception {
          EppInformation eppInformation = new EppInformation();

          eppInformation.setFileName(_file.getName());
          eppInformation.setEppType(EppInformation.EppType.Paragon);
          eppInformation.setId(_paragon.getId());
          eppInformation.setOrderName("ikea_paragon");
          eppInformation.setInvoiceName("paragon_" + _paragon.getId());

          APIRequest request = HttpServiceUtil.get("/epp/generate", eppInformation.getParams());
          URL url = request.getUrl().toURL();

          URLConnection con = url.openConnection();

          BufferedInputStream bis = new BufferedInputStream(con.getInputStream());

          Files.copy(bis, _file.toPath(), StandardCopyOption.REPLACE_EXISTING);

          return null;
        }
      };
    }

    public void setParagon(File file, Paragon paragon) {
      this.fileProperty.setValue(file);
      this.paragonProperty.setValue(paragon);
    }
  }


  class ParagonCancelService extends AbstractAsyncService<Void> {
    private LongProperty paragonId = new SimpleLongProperty();

    @Override
    protected Task<Void> createTask() {
      final Long _paragonId = paragonId.get();
      return new Task<Void>() {
        @Override
        protected Void call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/paragon/" + _paragonId + "/cancel");
          request.get();

          return null;
        }
      };
    }

    public void setParagonId(Long paragonId) {
      this.paragonId.setValue(paragonId);
    }
  }


  class LoadService extends AbstractAsyncService<PageResult<Paragon>> {
    private IntegerProperty pageIndex = new SimpleIntegerProperty();

    @Override
    protected Task<PageResult<Paragon>> createTask() {
      final int _pageIndex = pageIndex.get();

      return new Task<PageResult<Paragon>>() {
        @Override
        protected PageResult<Paragon> call() throws Exception {
          APIRequest apiRequest = HttpServiceUtil.get("/paragons/page/" + _pageIndex + "/" + ITEM_PER_PAGE);

          return apiRequest.getData(new TypeReference<PageResult<Paragon>>() {
          });
        }
      };
    }

    public void setPageIndex(int pageIndex) {
      this.pageIndex.set(pageIndex);
    }
  }

}
