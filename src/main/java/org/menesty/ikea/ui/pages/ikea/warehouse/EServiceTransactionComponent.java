package org.menesty.ikea.ui.pages.ikea.warehouse;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.eservice.EServiceTransaction;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.List;

/**
 * Created by Menesty on
 * 8/24/16.
 * 12:45.
 */
public class EServiceTransactionComponent extends BorderPane {
  private BaseTableView<EServiceTransaction> tableView;
  private LoadService loadService;
  private MarkAsUsedService markAsUsedService;

  public EServiceTransactionComponent() {
    tableView = new BaseTableView<>();

    {
      TableColumn<EServiceTransaction, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.TRANSACTION_ID));
      column.setMinWidth(150);
      column.setCellValueFactory(ColumnUtil.<EServiceTransaction, Number>column("transactionId"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<EServiceTransaction, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
      column.setCellValueFactory(ColumnUtil.bigDecimal("price"));
      column.setMinWidth(80);
      column.getStyleClass().add("align-right");
      tableView.getColumns().add(column);
    }

    {
      TableColumn<EServiceTransaction, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.CREATED_DATE));
      column.setCellValueFactory(ColumnUtil.dateColumn("date"));
      column.setMinWidth(120);
      column.getStyleClass().add("align-right");
      tableView.getColumns().add(column);
    }

    {
      TableColumn<EServiceTransaction, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ORDER_NAME));
      column.setCellValueFactory(ColumnUtil.<EServiceTransaction, String>column("orderName"));
      column.setMinWidth(150);
      column.getStyleClass().add("align-right");
      tableView.getColumns().add(column);
    }

    {
      TableColumn<EServiceTransaction, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.BUYER_NAME));
      column.setCellValueFactory(ColumnUtil.<EServiceTransaction, String>column("buyerName"));
      column.setMinWidth(150);
      column.getStyleClass().add("align-right");
      tableView.getColumns().add(column);
    }

    tableView.setRowRenderListener((row, newValue) -> {
      row.setContextMenu(null);

      if (newValue != null) {
        ContextMenu contextMenu = new ContextMenu();

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.MARK_AS_USED));
          menuItem.setOnAction(event -> {
            markAsUsedService.transactionIdProperty.setValue(newValue.getTransactionId());
            markAsUsedService.restart();
          });

          contextMenu.getItems().add(menuItem);
        }

        row.setContextMenu(contextMenu);
      }
    });

    StackPane main = new StackPane();
    LoadingPane loadingPane = new LoadingPane();

    main.getChildren().addAll(tableView, loadingPane);
    setCenter(main);


    loadService = new LoadService();
    loadService.setOnSucceededListener(data -> tableView.getItems().setAll(data));
    markAsUsedService = new MarkAsUsedService();
    markAsUsedService.setOnSucceededListener(data -> {
      if (data) {
        load();
      }
    });

    loadingPane.bindTask(loadService, markAsUsedService);
    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createReload32Icon());
      button.setOnAction(event -> load());

      toolBar.getItems().add(button);
    }

    setTop(toolBar);
  }

  public void load() {
    loadService.restart();
  }

  class LoadService extends AbstractAsyncService<List<EServiceTransaction>> {
    @Override
    protected Task<List<EServiceTransaction>> createTask() {
      return new Task<List<EServiceTransaction>>() {
        @Override
        protected List<EServiceTransaction> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/eservice-transaction/not-used");
          return request.getList(new TypeReference<List<EServiceTransaction>>() {
          });
        }
      };
    }
  }

  class MarkAsUsedService extends AbstractAsyncService<Boolean> {
    private LongProperty transactionIdProperty = new SimpleLongProperty();

    @Override
    protected Task<Boolean> createTask() {
      Long _transactionId = transactionIdProperty.get();
      return new Task<Boolean>() {
        @Override
        protected Boolean call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/eservice-transaction/" + _transactionId + "/set-used");
          return request.getData(Boolean.class);
        }
      };
    }
  }
}
