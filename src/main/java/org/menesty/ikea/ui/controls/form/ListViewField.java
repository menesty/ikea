package org.menesty.ikea.ui.controls.form;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.factory.ImageFactory;

import java.util.List;

/**
 * Created by Menesty on
 * 6/20/14.
 * 18:56.
 */
public class ListViewField extends VBox implements Field {

    private ListView<String> listView;

    private String label;

    private boolean allowBlank;
    private TextField editField;

    public ListViewField(String label, boolean allowBlank) {
        this.label = label;
        this.allowBlank = allowBlank;

        listView = new ListView<>();
        editField = new TextField();

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(5, 0, 5, 0));
        hBox.setSpacing(5);
        hBox.getChildren().add(editField);

        {
            Button button = new Button(null, ImageFactory.createAdd16Icon());
            button.setOnAction(actionEvent -> {
                String value = editField.getText().trim();

                if (StringUtils.isNotBlank(value) && !listView.getItems().contains(value)) {
                    listView.getItems().add(value);
                    editField.setText("");
                }
            });
            hBox.getChildren().add(button);
        }

        {
            Button button = new Button(null, ImageFactory.createDelete16Icon());
            button.setOnAction(actionEvent -> {
                String item = listView.getSelectionModel().getSelectedItem();

                if (item != null) {
                    listView.getItems().remove(item);
                }
            });
            hBox.getChildren().add(button);
        }

        getChildren().addAll(listView, hBox);
    }

    public void setValue(List<String> value) {
        listView.getItems().clear();

        if (value == null || value.isEmpty()) {
            return;
        }

        listView.getItems().addAll(value);
    }

    public List<String> getValues() {
        return listView.getItems();
    }

    @Override
    public boolean isValid() {
        boolean result = true;
        getStyleClass().removeAll("validation-succeed", "validation-error");

        if (!allowBlank)
            setValid(result = listView.getItems().size() != 0);

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
        listView.getItems().clear();
        editField.clear();

        listView.getStyleClass().removeAll("validation-succeed", "validation-error");
        listView.getStyleClass().add("gray-border");
    }

    @Override
    public String getLabel() {
        return label;
    }
}
