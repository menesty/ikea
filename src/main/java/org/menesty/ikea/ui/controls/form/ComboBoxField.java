package org.menesty.ikea.ui.controls.form;

import com.sun.javafx.scene.control.skin.AutoCompleteComboBoxListViewSkin;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import org.menesty.ikea.ui.controls.form.provider.DataProvider;
import org.menesty.ikea.util.ToolTipUtil;

import java.util.*;

/**
 * Created by Menesty on
 * 9/1/15.
 * 09:12.
 */
public class ComboBoxField<T> extends HBox implements Field {
  private String label;
  private ComboBox<T> comboBox;
  private ProgressIndicator indicator;
  private DataProvider<T> dataProvider;
  private ItemLabel<T> itemLabel = Object::toString;
  private Map<String, Integer> itemFastMap = new HashMap<>();
  private int selectedIndex = -1;
  private boolean allowBlank = true;
  private int minLength = -1;

  public ComboBoxField(String label) {
    this.label = label;
    this.comboBox = new ComboBox<>();
    this.comboBox.setSkin(new AutoCompleteComboBoxListViewSkin<>(comboBox));

    HBox.setHgrow(comboBox, Priority.ALWAYS);

    indicator = new ProgressIndicator();
    indicator.setMaxHeight(23);
    indicator.setVisible(false);

    getChildren().addAll(comboBox, indicator);
  }

  public void setAllowBlank(boolean allowBlank) {
    this.allowBlank = allowBlank;
  }

  @Override
  public boolean isValid() {
    boolean result = true;
    comboBox.getStyleClass().removeAll("validation-succeed", "validation-error");

    if (!allowBlank) {
      setValid(result = getValue() != null);
      comboBox.getStyleClass().remove("white-border");
    }

    return result;
  }


  public void setValid(boolean valid) {
    comboBox.getStyleClass().removeAll("validation-succeed", "validation-error");
    comboBox.getStyleClass().remove("white-border");

    if (valid)
      comboBox.getStyleClass().add("validation-succeed");
    else
      comboBox.getStyleClass().add("validation-error");

  }

  @Override
  public void reset() {
    comboBox.setValue(null);
  }

  public List<T> getItems() {
    return Collections.unmodifiableList(comboBox.getItems());
  }

  @Override
  public String getLabel() {
    return label;
  }

  public void setEditable(boolean editable) {
    comboBox.setEditable(editable);

    if (dataProvider == null) {
      dataProvider = new DataProvider<>();
      dataProvider.setItemLabel(itemLabel);
    }

    comboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      selectedIndex = -1;
      String filterText = comboBox.getEditor().getText();

      if (filterText == null || filterText.trim().equals("")) {
        return;
      }

      if (itemFastMap.containsKey(filterText)) {
        selectedIndex = itemFastMap.get(filterText);
        return;
      }

      if (minLength != -1 && filterText.trim().length() < minLength) {
        return;
      }

      comboBox.show();
      indicator.setVisible(true);


      dataProvider.filter(new DataProvider.CallBack<T>() {
        @Override
        public void onData(List<T> data) {
          setItems(data);
          indicator.setVisible(false);
        }

        @Override
        public void onError() {
          indicator.setVisible(false);
        }
      }, filterText);
    });
  }

  public void addSelectItemListener(ChangeListener<? super T> listener) {
    comboBox.getSelectionModel().selectedItemProperty().addListener(listener);
  }

  public void setItemLabel(ItemLabel<T> itemLabel) {
    this.itemLabel = itemLabel;

    comboBox.setCellFactory(param ->
            new ListCell<T>() {
              @Override
              protected void updateItem(T t, boolean bln) {
                super.updateItem(t, bln);

                if (t != null) {
                  setText(itemLabel.label(t));
                }
              }

            }
    );

    comboBox.setConverter(new StringConverter<T>() {
      @Override
      public String toString(T item) {
        return item == null ? comboBox.getEditor().getText() : itemLabel.label(item);
      }

      @Override
      public T fromString(String string) {
        Integer index = itemFastMap.get(string);

        if (index != null) {
          selectedIndex = index;
          return comboBox.getItems().get(selectedIndex);
        }

        return null;
      }
    });

    if (dataProvider != null) {
      dataProvider.setItemLabel(itemLabel);
    }

    buildFastMap();
  }

  @SafeVarargs
  public final void setItems(T... items) {
    setItems(Arrays.asList(items));
  }

  public void setItems(List<T> items) {
    updateItems(items);
  }

  public void setValue(T value) {
    comboBox.setValue(value);

    if (value != null) {
      if (comboBox.getItems().isEmpty() || !comboBox.getItems().contains(value)) {
        comboBox.getItems().add(value);
      }

      selectedIndex = comboBox.getItems().indexOf(value);
    } else {
      selectedIndex = -1;
    }
  }

  public T getValue() {
    if (comboBox.isEditable()) {
      return selectedIndex != -1 ? comboBox.getItems().get(selectedIndex) : null;
    }

    return comboBox.getSelectionModel().getSelectedItem();
  }

  private void updateItems(List<T> items) {
    comboBox.getItems().setAll(items);
    buildFastMap();
  }

  private void buildFastMap() {
    itemFastMap.clear();

    for (int index = 0; index < comboBox.getItems().size(); index++) {
      itemFastMap.put(itemLabel.label(comboBox.getItems().get(index)), index);
    }
  }

  public void setLoader(DataProvider<T> dataProvider) {
    setLoader(dataProvider, -1);
  }

  public void setLoader(DataProvider<T> dataProvider, int minLength) {
    this.minLength = minLength;
    this.dataProvider = dataProvider;
  }

  public ReadOnlyObjectProperty<T> selectedItemProperty() {
    return comboBox.getSelectionModel().selectedItemProperty();
  }

  public void setTooltip(String tooltip) {
    comboBox.setTooltip(ToolTipUtil.create(tooltip));
  }
}

