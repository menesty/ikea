package org.menesty.ikea.ui.pages.ikea.warehouse.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.eservice.EServiceTransaction;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Menesty on
 * 8/10/16.
 * 12:50.
 */
public class EServiceTransactionDialog extends BaseDialog {
  private BaseTableView<EServiceTransaction> tableView;
  private LoadService loadService;
  private EntityDialogCallback<EServiceTransaction> entityDialogCallback;

  public EServiceTransactionDialog(Stage stage) {
    super(stage);

    setMaxWidth(700);

    BorderPane main = new BorderPane();

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


    main.setCenter(tableView);

    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> tableView.getItems().setAll(value));

    LoadingPane loadingPane = new LoadingPane();
    loadingPane.bindTask(loadService);

    StackPane stackPane = new StackPane(main, loadingPane);
    addRow(stackPane, bottomBar);

  }

  @Override
  public void onCancel() {
    if (entityDialogCallback != null) {
      entityDialogCallback.onCancel();
    }
  }

  @Override
  public void onOk() {
    if (entityDialogCallback != null) {
      EServiceTransaction selectedContragent = tableView.getSelectionModel().getSelectedItem();
      entityDialogCallback.onSave(selectedContragent);
    }
  }

  public void bind(BigDecimal amount, EntityDialogCallback<EServiceTransaction> entityDialogCallback) {
    this.entityDialogCallback = entityDialogCallback;
    loadService.setAmount(amount);
  }


  @Override
  public void onShow() {
    loadService.restart();
  }

  class LoadService extends AbstractAsyncService<List<EServiceTransaction>> {
    private ObjectProperty<BigDecimal> amountProperty = new SimpleObjectProperty<>();

    @Override
    protected Task<List<EServiceTransaction>> createTask() {
      BigDecimal _amount = amountProperty.get();
      return new Task<List<EServiceTransaction>>() {
        @Override
        protected List<EServiceTransaction> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/eservice-transaction/nearby/transaction/" + _amount);

          return request.getList(new TypeReference<List<EServiceTransaction>>() {
          });
        }
      };
    }

    public void setAmount(BigDecimal amount) {
      amountProperty.setValue(amount);
    }
  }

}
