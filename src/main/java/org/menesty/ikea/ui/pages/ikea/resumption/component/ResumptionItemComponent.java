package org.menesty.ikea.ui.pages.ikea.resumption.component;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.ResumptionItem;
import org.menesty.ikea.lib.dto.PageResult;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.pages.ikea.resumption.dialog.ResumptionItemAddDialog;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;
import org.menesty.ikea.util.PaginationUtil;

/**
 * Created by Menesty on
 * 3/17/16.
 * 16:10.
 */
public class ResumptionItemComponent extends StackPane {
  private LoadService loadService;
  private ResumptionItemTableView tableView;
  private Pagination pagination;

  public ResumptionItemComponent(DialogSupport dialogSupport) {
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
    {
      Button button = new Button(null, ImageFactory.createAdd32Icon());
      button.setOnAction(event -> {
        ResumptionItemAddDialog dialog = new ResumptionItemAddDialog(dialogSupport.getStage()) {
          @Override
          public void onCancel() {
            dialogSupport.hidePopupDialog();
          }
        };

        dialogSupport.showPopupDialog(dialog);
      });

      toolBar.getItems().add(button);
    }

    main.setTop(toolBar);
    main.setCenter(tableView = new ResumptionItemTableView());

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SHOPS));
      column.setCellValueFactory(ColumnUtil.column("resumptionShop"));
      column.setPrefWidth(100);
      tableView.getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.RESUMPTION_INVOICE));

      column.setCellValueFactory(ColumnUtil.column("resumptionInvoice"));
      column.setMinWidth(50);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ResumptionItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.RESUMPTION_DATE));
      column.setCellValueFactory(ColumnUtil.dateColumn("resumptionDate"));
      column.setPrefWidth(140);
      tableView.getColumns().add(column);
    }


    pagination = new Pagination(1, 0);

    pagination.currentPageIndexProperty().addListener((observable, oldValue, pageIndex) -> {
      loadService.setPage(PaginationUtil.getPageNumber(pageIndex.intValue()));
      loadService.restart();
    });

    main.setBottom(pagination);

    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> {
      tableView.getItems().setAll(value.getData());
      pagination.setPageCount(PaginationUtil.getPageCount(value.getCount(), value.getLimit()));
    });

    LoadingPane loadingPane = new LoadingPane();
    loadingPane.bindTask(loadService);

    getChildren().addAll(main, loadingPane);
  }

  public void load() {
    loadService.setPage(1);
    loadService.restart();
  }

  class LoadService extends AbstractAsyncService<PageResult<ResumptionItem>> {
    private IntegerProperty pageProperty = new SimpleIntegerProperty();

    @Override
    protected Task<PageResult<ResumptionItem>> createTask() {
      final int _page = pageProperty.get();

      return new Task<PageResult<ResumptionItem>>() {
        @Override
        protected PageResult<ResumptionItem> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/resumption/items/page/" + _page);

          return request.getData(new TypeReference<PageResult<ResumptionItem>>() {
          });
        }
      };
    }

    public void setPage(int page) {
      this.pageProperty.set(page);
    }
  }
}
