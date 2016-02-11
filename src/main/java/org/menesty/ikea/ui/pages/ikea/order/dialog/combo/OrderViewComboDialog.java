package org.menesty.ikea.ui.pages.ikea.order.dialog.combo;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.form.NumberTextField;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Menesty on
 * 11/27/15.
 * 00:03.
 */
public class OrderViewComboDialog extends BaseDialog {
  private TableView<IkeaProduct> comboTableView;
  private LoadService loadService;
  private NumberTextField countField;
  private EntityDialogCallback<ComboSelectResult> callback;
  private String partArtNumber;

  public OrderViewComboDialog(Stage stage) {
    super(stage);
    setTitle(I18n.UA.getString(I18nKeys.CHOICE_COMBO_DIALOG));
    setAllowAutoHide(false);

    comboTableView = new TableView<>();

    {
      TableColumn<IkeaProduct, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setPrefWidth(125);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);

      comboTableView.getColumns().add(column);
    }

    {
      TableColumn<IkeaProduct, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SHORT_NAME));
      column.setPrefWidth(300);
      column.setCellValueFactory(ColumnUtil.column("shortName"));

      comboTableView.getColumns().add(column);
    }

    comboTableView.setPrefHeight(200);

    LoadingPane loadingPane = new LoadingPane();

    StackPane main = new StackPane();

    main.getChildren().addAll(comboTableView, loadingPane);
    addRow(main);

    HBox hBox = new HBox(4);
    Label label;
    hBox.getChildren().addAll(label = new Label(I18n.UA.getString(I18nKeys.COUNT)), countField = new NumberTextField(null, true));
    label.setMaxHeight(24);
    addRow(hBox);

    countField.setAllowBlank(false);
    countField.setAllowDouble(false);

    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> comboTableView.getItems().setAll(value));

    loadingPane.bindTask(loadService);

    setMaxWidth(500);

    addRow(bottomBar);
  }

  @Override
  public void onCancel() {
    if (callback != null)
      callback.onCancel();
  }

  @Override
  public void onOk() {
    if (countField.isValid()) {
      IkeaProduct selectedCombo = comboTableView.getSelectionModel().getSelectedItem();
      int count = countField.getNumber().intValue();

      if (selectedCombo != null && count != 0) {
        callback.onSave(new ComboSelectResult(selectedCombo.getId(), partArtNumber, count));
      }
    }
  }

  public void loadCombos(Long ikeaProcessOrderId, String partArtNumber, EntityDialogCallback<ComboSelectResult> callback) {
    this.callback = callback;
    this.partArtNumber = partArtNumber;

    comboTableView.getItems().clear();
    countField.reset();

    loadService.setIkeaProcessOrder(ikeaProcessOrderId);
    loadService.restart();
  }

  class LoadService extends AbstractAsyncService<List<IkeaProduct>> {
    private LongProperty ikeaProcessOrderProperty = new SimpleLongProperty();

    @Override
    protected Task<List<IkeaProduct>> createTask() {
      final Long _ikeaProcessOrderId = ikeaProcessOrderProperty.get();

      return new Task<List<IkeaProduct>>() {
        @Override
        protected List<IkeaProduct> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/ikea-order/combo/" + _ikeaProcessOrderId);
          return request.getList(new TypeReference<List<IkeaProduct>>() {
          });
        }
      };
    }

    public void setIkeaProcessOrder(Long ikeaProcessOrderId) {
      ikeaProcessOrderProperty.setValue(ikeaProcessOrderId);
    }
  }

  public static class ComboSelectResult {
    private final Long comboId;
    private final String partArtNumber;
    private final int count;

    public ComboSelectResult(Long comboId, String partArtNumber, int count) {
      this.comboId = comboId;
      this.partArtNumber = partArtNumber;
      this.count = count;
    }

    public int getCount() {
      return count;
    }

    public Long getComboId() {
      return comboId;
    }

    public String getPartArtNumber() {
      return partArtNumber;
    }
  }
}
