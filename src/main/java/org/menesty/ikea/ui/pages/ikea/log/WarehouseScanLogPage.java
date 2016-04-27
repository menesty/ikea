package org.menesty.ikea.ui.pages.ikea.log;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.warehouse.WarehouseProductScanLog;
import org.menesty.ikea.lib.dto.PageResult;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;
import org.menesty.ikea.util.PaginationUtil;

/**
 * Created by Menesty on
 * 3/27/16.
 * 19:39.
 */
public class WarehouseScanLogPage extends BasePage {
  private LoadService loadService;
  private Pagination pagination;
  private TableView<WarehouseProductScanLog> tableView;

  @Override
  protected void initialize() {
    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> {
      tableView.getItems().setAll(value.getData());
      pagination.setPageCount(PaginationUtil.getPageCount(value.getCount(), value.getLimit()));
    });

  }

  @Override
  public void onActive(Object... params) {
    loadingPane.bindTask(loadService);
    loadService.setPage(1);
    loadService.restart();
  }

  @Override
  protected Node createView() {
    BorderPane main = new BorderPane();

    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createReload32Icon());
      button.setOnAction(event -> {
        loadService.setPage(1);
        loadService.restart();
      });

      toolBar.getItems().add(button);
    }
    
    main.setTop(toolBar);

    tableView = new TableView<>();

    {
      TableColumn<WarehouseProductScanLog, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setMinWidth(140);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<WarehouseProductScanLog, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SCAN_DATE));
      column.setMinWidth(130);
      column.setCellValueFactory(ColumnUtil.dateColumn("createdDate"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<WarehouseProductScanLog, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.CLIENT));
      column.setMinWidth(100);
      column.setCellValueFactory(param -> {
        WarehouseProductScanLog value = param.getValue();

        if (value == null) {
          return null;
        }

        return new SimpleStringProperty(value.getFistName() + " " + value.getLastName());
      });

      tableView.getColumns().add(column);
    }

    {
      TableColumn<WarehouseProductScanLog, String> column = new TableColumn<>();
      column.setMinWidth(60);
      column.setCellValueFactory(param -> {
        WarehouseProductScanLog value = param.getValue();

        if (value == null) {
          return null;
        }

        return new SimpleStringProperty(value.isExpected() ? I18n.UA.getString(I18nKeys.YES) : I18n.UA.getString(I18nKeys.NO));
      });

      tableView.getColumns().add(column);
    }

    main.setCenter(tableView);

    pagination = new Pagination(1, 0);

    pagination.currentPageIndexProperty().addListener((observable, oldValue, pageIndex) -> {
      loadService.setPage(PaginationUtil.getPageNumber(pageIndex.intValue()));
      loadService.restart();
    });

    main.setBottom(pagination);

    return wrap(main);
  }

  class LoadService extends AbstractAsyncService<PageResult<WarehouseProductScanLog>> {
    private IntegerProperty pageProperty = new SimpleIntegerProperty();

    @Override
    protected Task<PageResult<WarehouseProductScanLog>> createTask() {
      final int _page = pageProperty.get();
      return new Task<PageResult<WarehouseProductScanLog>>() {
        @Override
        protected PageResult<WarehouseProductScanLog> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/warehouse/scan/log/" + _page);

          return request.getData(new TypeReference<PageResult<WarehouseProductScanLog>>() {
          });
        }
      };
    }

    public void setPage(int page) {
      this.pageProperty.set(page);
    }
  }
}
