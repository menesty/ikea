package org.menesty.ikea.ui.pages.ikea.order.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.apache.http.client.methods.HttpDelete;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ClientOrder;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.List;

/**
 * Created by Menesty on
 * 3/1/16.
 * 17:16.
 */
public class OrderPartsDetailDialog extends BaseDialog {
  private TableView<ClientOrder> clientOrderTableView;
  private LoadService loadService;
  private DeleteClientOrderService deleteClientOrderService;
  private EntityDialogCallback<Boolean> entityDialogCallback;
  private boolean state = false;

  public OrderPartsDetailDialog(Stage stage) {
    super(stage);
    setAllowAutoHide(false);

    clientOrderTableView = new TableView<>();

    {
      TableColumn<ClientOrder, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.CLIENT));
      column.setMinWidth(200);
      column.setCellValueFactory(param -> {
        ClientOrder clientOrder = param.getValue();

        if (clientOrder != null) {
          return new SimpleStringProperty(clientOrder.getProfile().getLastName() + " " + clientOrder.getProfile().getFirstName());
        }

        return null;
      });

      clientOrderTableView.getColumns().add(column);
    }

    {
      TableColumn<ClientOrder, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.AMOUNT));
      column.setMinWidth(100);
      column.setCellValueFactory(ColumnUtil.number("amount"));

      clientOrderTableView.getColumns().add(column);
    }

    {
      TableColumn<ClientOrder, Long> column = new TableColumn<>();
      column.setCellValueFactory(ColumnUtil.column("id"));
      column.setCellFactory(param -> new TableCell<ClientOrder, Long>() {
        @Override
        protected void updateItem(Long item, boolean empty) {
          super.updateItem(item, empty);

          if (empty) {
            setGraphic(null);
            setText(null);
          } else {
            Button button = new Button(null, ImageFactory.createDelete16Icon());

            button.setOnAction(event -> {
              deleteClientOrderService.setData(loadService.ikeaProcessOrderIdProperty.get(), item);
              deleteClientOrderService.restart();
            });

            setGraphic(button);
          }
        }
      });

      clientOrderTableView.getColumns().add(column);
    }

    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> clientOrderTableView.getItems().setAll(value));

    deleteClientOrderService = new DeleteClientOrderService();
    deleteClientOrderService.setOnSucceededListener(value -> {
      if (value) {
        state = true;
        loadService.restart();
      }
    });

    cancelBtn.setVisible(false);
    addRow(clientOrderTableView, bottomBar);
  }

  public void bind(Long ikeaProcessOrderId, EntityDialogCallback<Boolean> entityDialogCallback) {
    state = false;
    loadService.ikeaProcessOrderIdProperty.setValue(ikeaProcessOrderId);
    this.entityDialogCallback = entityDialogCallback;
  }

  @Override
  public void onOk() {
    entityDialogCallback.onSave(state);
  }

  @Override
  public void onShow() {
    loadingPane.bindTask(loadService, deleteClientOrderService);

    loadService.restart();
  }

  class LoadService extends AbstractAsyncService<List<ClientOrder>> {
    private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();

    @Override
    protected Task<List<ClientOrder>> createTask() {
      final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();

      return new Task<List<ClientOrder>>() {
        @Override
        protected List<ClientOrder> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/ikea-order/" + _ikeaProcessOrderId + "/client-orders");

          return request.getList(new TypeReference<List<ClientOrder>>() {
          });
        }
      };
    }
  }

  class DeleteClientOrderService extends AbstractAsyncService<Boolean> {
    private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();
    private LongProperty clientOrderIdProperty = new SimpleLongProperty();

    @Override
    protected Task<Boolean> createTask() {
      final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();
      final Long _clientOrderId = clientOrderIdProperty.get();

      return new Task<Boolean>() {
        @Override
        protected Boolean call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/ikea-order/" + _ikeaProcessOrderId + "/client-order/" + _clientOrderId + "/delete");
          return request.getData(Boolean.class, HttpDelete.METHOD_NAME);
        }
      };
    }

    public void setData(Long ikeaProcessOrderId, Long clientOrderId) {
      ikeaProcessOrderIdProperty.setValue(ikeaProcessOrderId);
      clientOrderIdProperty.setValue(clientOrderId);
    }
  }
}
