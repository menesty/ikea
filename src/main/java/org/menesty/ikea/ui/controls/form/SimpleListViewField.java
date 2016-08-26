package org.menesty.ikea.ui.controls.form;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Created by Menesty on
 * 7/4/16.
 * 12:48.
 */
public class SimpleListViewField<T> extends VBox implements Field {

  private ListView<T> listView;

  private String label;

  private boolean allowBlank;

  public SimpleListViewField(String label, boolean allowBlank) {
    this.label = label;
    this.allowBlank = allowBlank;

    listView = new ListView<>();
    listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    getChildren().add(listView);
  }

  public void setAllowBlank(boolean allowBlank) {
    this.allowBlank = allowBlank;
  }

  public List<T> getSelected() {
    return listView.getSelectionModel().getSelectedItems();
  }

  public void setLabel(ItemLabel<T> itemLabel) {
    listView.setCellFactory(param -> new ListCell<T>() {
      @Override
      public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
          setGraphic(null);
          setText(null);
        } else {
          setText(itemLabel.label(item));
        }
      }
    });
  }

  @Override
  public boolean isValid() {
    boolean result = true;
    getStyleClass().removeAll("validation-succeed", "validation-error");

    if (!allowBlank) {
      setValid(result = !getSelected().isEmpty());
    }

    return result;
  }

  public void setValid(boolean valid) {
    listView.getStyleClass().removeAll("validation-succeed", "validation-error");
    listView.getStyleClass().remove("gray-border");

    if (valid)
      listView.getStyleClass().add("validation-succeed");
    else
      listView.getStyleClass().add("validation-error");

  }

  @Override
  public void reset() {
    listView.getSelectionModel().clearSelection();

    listView.getStyleClass().removeAll("validation-succeed", "validation-error");
    listView.getStyleClass().add("gray-border");
  }

  @Override
  public String getLabel() {
    return label;
  }

  public void setItems(List<T> value) {
    listView.getItems().setAll(value);
  }
}
