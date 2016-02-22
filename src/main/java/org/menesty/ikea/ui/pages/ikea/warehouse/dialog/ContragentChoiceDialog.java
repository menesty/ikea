package org.menesty.ikea.ui.pages.ikea.warehouse.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.Contragent;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 2/18/16.
 * 18:42.
 */
public class ContragentChoiceDialog extends BaseDialog {
  private List<Contragent> items;
  private BaseTableView<Contragent> tableView;
  private LoadService loadService;
  private EntityDialogCallback<Contragent> entityDialogCallback;

  public ContragentChoiceDialog(Stage stage) {
    super(stage);
    setMaxWidth(600);

    BorderPane main = new BorderPane();

    tableView = new BaseTableView<>();

    {
      TableColumn<Contragent, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.FIRST_NAME));
      column.setMinWidth(150);
      column.setCellValueFactory(ColumnUtil.<Contragent, String>column("firstName"));
      tableView.getColumns().add(column);
    }
    {
      TableColumn<Contragent, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.LAST_NAME));
      column.setCellValueFactory(ColumnUtil.<Contragent, String>column("lastName"));
      column.setMinWidth(150);
      column.getStyleClass().add("align-right");
      tableView.getColumns().add(column);
    }

    {
      TableColumn<Contragent, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.DOCUMENT_NUMBER));
      column.setCellValueFactory(ColumnUtil.<Contragent, String>column("documentNumber"));
      column.setMinWidth(150);
      column.getStyleClass().add("align-right");
      tableView.getColumns().add(column);
    }

    VBox topPanel = new VBox();
    topPanel.setPadding(new Insets(0, 0, 3, 0));

    {
      final TextField artNumberField = new TextField();

      artNumberField.setDelay(1);
      artNumberField.setOnDelayAction(actionEvent -> filter(artNumberField.getText()));
      artNumberField.setPromptText("Product ID #");

      topPanel.getChildren().add(artNumberField);
    }

    main.setTop(topPanel);
    main.setCenter(tableView);

    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> {
      items = value;
      tableView.getItems().setAll(items);
    });

    LoadingPane loadingPane = new LoadingPane();
    loadingPane.bindTask(loadService);

    StackPane stackPane = new StackPane(main, loadingPane);
    addRow(stackPane, bottomBar);
  }

  @Override
  public void onShow() {
    loadService.restart();
  }

  private void filter(final String text) {
    List<Contragent> filtered = items;

    if (StringUtils.isNotBlank(text)) {
      filtered = items.stream()
          .filter(contragent -> contragent.getFirstName().contains(text) || contragent.getLastName().contains(text))
          .collect(Collectors.toList());
    }

    tableView.getItems().setAll(filtered);
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
      Contragent selectedContragent = tableView.getSelectionModel().getSelectedItem();
      entityDialogCallback.onSave(selectedContragent);
    }
  }

  public void bind(EntityDialogCallback<Contragent> entityDialogCallback) {
    this.entityDialogCallback = entityDialogCallback;
  }

  class LoadService extends AbstractAsyncService<List<Contragent>> {

    @Override
    protected Task<List<Contragent>> createTask() {
      return new Task<List<Contragent>>() {
        @Override
        protected List<Contragent> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/contragents");

          return request.getList(new TypeReference<List<Contragent>>() {
          });
        }
      };
    }
  }

}
