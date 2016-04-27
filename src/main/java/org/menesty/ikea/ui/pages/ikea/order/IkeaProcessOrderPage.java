package org.menesty.ikea.ui.pages.ikea.order;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.order.IkeaProcessOrder;
import org.menesty.ikea.lib.dto.PageResult;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.DialogCallback;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.ikea.order.dialog.OrderPartsDetailDialog;
import org.menesty.ikea.ui.pages.wizard.order.OrderCreateWizardPage;
import org.menesty.ikea.util.*;

/**
 * Created by Menesty on
 * 9/28/15.
 * 19:05.
 */
public class IkeaProcessOrderPage extends BasePage {
  private BaseTableView<IkeaProcessOrder> tableView;
  private LoadService loadService;
  private DeleteService deleteService;
  private Pagination pagination;
  private OrderPartsDetailDialog orderPartsDetailDialog;

  @Override
  protected void initialize() {
    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> {
      tableView.getItems().setAll(value.getData());
      pagination.setPageCount(PaginationUtil.getPageCount(value.getCount(), value.getLimit()));
    });

    deleteService = new DeleteService();
    deleteService.setOnSucceededListener(value -> {
      if (value != null) {
        Dialog.alert(getDialogSupport(), I18n.UA.getString(I18nKeys.WARNING), I18n.UA.getString(value));
      } else {
        loadService.restart();
      }
    });
  }

  @Override
  protected Node createView() {
    BorderPane main = new BorderPane();

    tableView = new BaseTableView<>();

    {
      TableColumn<IkeaProcessOrder, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ID));
      column.setMinWidth(60);
      column.setCellValueFactory(ColumnUtil.<IkeaProcessOrder, String>column("id"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<IkeaProcessOrder, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.NAME));
      column.setMinWidth(200);
      column.setPrefWidth(250);
      column.setCellValueFactory(ColumnUtil.<IkeaProcessOrder, String>column("name"));
      tableView.getColumns().add(column);
    }
    {
      TableColumn<IkeaProcessOrder, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.AMOUNT));
      column.setCellValueFactory(ColumnUtil.<IkeaProcessOrder>number("amount"));
      column.setMinWidth(100);
      column.getStyleClass().add("align-right");
      tableView.getColumns().add(column);
    }

    {
      TableColumn<IkeaProcessOrder, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.UPDATED_DATE));
      column.setCellValueFactory(ColumnUtil.<IkeaProcessOrder>dateColumn("updatedDate"));
      column.setMinWidth(150);
      column.getStyleClass().add("align-right");
      tableView.getColumns().add(column);
    }

    tableView.setRowRenderListener((row, newValue) -> {
      row.setContextMenu(null);

      if (newValue != null) {
        ContextMenu contextMenu = new ContextMenu();

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.DELETE), ImageFactory.createDelete16Icon());
          menuItem.setOnAction(event -> {
            Dialog.confirm(getDialogSupport(), I18n.UA.getString(I18nKeys.WARNING), I18n.UA.getString(I18nKeys.DELETE_CONFIRMATION_MESSAGE), new DialogCallback() {
              @Override
              public void onCancel() {

              }

              @Override
              public void onYes() {
                deleteService.setIkeaProcessOrderId(newValue.getId());
                deleteService.restart();
              }
            });
          });

          contextMenu.getItems().add(menuItem);
        }

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.INFO), ImageFactory.createInfo22Icon());
          menuItem.setOnAction(event -> {
            OrderPartsDetailDialog orderPartsDetailDialog = getOrderPartsDetailDialog();
            orderPartsDetailDialog.bind(newValue.getId(), new EntityDialogCallback<Boolean>() {
              @Override
              public void onSave(Boolean reload, Object... params) {
                getDialogSupport().hidePopupDialog();
                if (reload) {
                  loadService.restart();
                }

              }

              @Override
              public void onCancel() {
                getDialogSupport().hidePopupDialog();
              }
            });

            getDialogSupport().showPopupDialog(orderPartsDetailDialog);
          });

          contextMenu.getItems().add(menuItem);
        }

        row.setContextMenu(contextMenu);
      }
    });

    main.setCenter(tableView);

    pagination = new Pagination(1, 0);

    pagination.currentPageIndexProperty().addListener((observable, oldValue, pageIndex) -> {
      loadService.setPage(PaginationUtil.getPageNumber(pageIndex.intValue()));
      loadService.restart();
    });

    main.setBottom(pagination);

    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.creteInfo48Icon());

      button.setOnAction(event -> navigateSubPage(IkeaOrderViewPage.class, tableView.getSelectionModel().getSelectedItem()));
      button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.INFO)));
      button.setDisable(true);

      tableView.getSelectionModel()
          .selectedItemProperty()
          .addListener((observable, oldValue, newValue) -> button.setDisable(newValue == null));
      toolBar.getItems().add(button);
    }

    {
      Button button = new Button(null, ImageFactory.createWizard48Icon());

      button.setOnAction(event -> navigateSubPage(OrderCreateWizardPage.class));
      button.setTooltip(new Tooltip(I18n.UA.getString(I18nKeys.CREATE_ORDER_WIZARD)));

      toolBar.getItems().add(button);
    }

    main.setTop(toolBar);

    return wrap(main);
  }

  @Override
  public void onActive(Object... params) {
    loadingPane.bindTask(loadService, deleteService);
    loadService.setPage(1);
    loadService.restart();
  }

  private OrderPartsDetailDialog getOrderPartsDetailDialog() {
    if (orderPartsDetailDialog == null) {
      orderPartsDetailDialog = new OrderPartsDetailDialog(getStage());
    }

    return orderPartsDetailDialog;
  }
}

class LoadService extends AbstractAsyncService<PageResult<IkeaProcessOrder>> {
  private IntegerProperty pageProperty = new SimpleIntegerProperty();

  @Override
  protected Task<PageResult<IkeaProcessOrder>> createTask() {
    final int _page = pageProperty.get();
    return new Task<PageResult<IkeaProcessOrder>>() {
      @Override
      protected PageResult<IkeaProcessOrder> call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/ikea-process-order/page/" + _page);

        return request.getData(new TypeReference<PageResult<IkeaProcessOrder>>() {
        });
      }
    };
  }

  public void setPage(int page) {
    this.pageProperty.set(page);
  }
}

class DeleteService extends AbstractAsyncService<String> {
  private LongProperty ikeaProcessOrderProperty = new SimpleLongProperty();

  @Override
  protected Task<String> createTask() {
    final Long _ikeaProcessOrderId = ikeaProcessOrderProperty.get();
    return new Task<String>() {
      @Override
      protected String call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/ikea-order/" + _ikeaProcessOrderId + "/remove");

        return request.getRawData();
      }
    };

  }

  public void setIkeaProcessOrderId(Long ikeaProcessOrderId) {
    this.ikeaProcessOrderProperty.setValue(ikeaProcessOrderId);
  }
}
