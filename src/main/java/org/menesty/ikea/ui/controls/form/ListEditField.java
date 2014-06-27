package org.menesty.ikea.ui.controls.form;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.menesty.ikea.factory.ImageFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty on
 * 6/20/14.
 * 18:56.
 */
public class ListEditField<T> extends VBox implements Field {
    private ListView<T> listView;

    private ComboBox<T> choiceComboBox;

    private List<T> choiceList;

    private List<T> values;

    private String label;

    private boolean allowBlank;

    public ListEditField(String label, boolean allowBlank) {
        this.label = label;
        this.allowBlank = allowBlank;

        listView = new ListView<>();

        choiceComboBox = new ComboBox<>();

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(5, 0, 5, 0));
        hBox.setSpacing(5);
        hBox.getChildren().add(choiceComboBox);

        {
            Button button = new Button(null, ImageFactory.createAdd16Icon());
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    T item = choiceComboBox.getSelectionModel().getSelectedItem();

                    if (item != null) {
                        choiceComboBox.getItems().remove(item);
                        listView.getItems().add(item);
                    }
                }
            });
            hBox.getChildren().add(button);
        }

        {
            Button button = new Button(null, ImageFactory.createDelete16Icon());
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    T item = listView.getSelectionModel().getSelectedItem();

                    if (item != null) {
                        listView.getItems().remove(item);
                        choiceComboBox.getItems().add(item);
                    }
                }
            });
            hBox.getChildren().add(button);
        }


        getChildren().addAll(listView, hBox);

    }

    public void setChoiceList(List<T> choiceList) {
        this.choiceList = new ArrayList<>(choiceList);

        choiceComboBox.getItems().clear();
        choiceComboBox.getItems().addAll(this.choiceList);

        updateChoiceList();
    }

    public void setValue(List<T> value) {
        values = (value == null ? new ArrayList<T>() : new ArrayList<>(value));
        listView.getItems().clear();
        listView.getItems().addAll(values);

        updateChoiceList();
    }

    private void updateChoiceList() {
        if (values != null)
            choiceComboBox.getItems().removeAll(values);
    }

    public List<T> getValues() {
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
        choiceComboBox.getItems().clear();

        if (values != null)
            values.clear();

        if (choiceList != null)
            choiceList.clear();

        listView.getStyleClass().removeAll("validation-succeed", "validation-error");
        listView.getStyleClass().add("gray-border");
    }

    @Override
    public String getLabel() {
        return label;
    }
}
