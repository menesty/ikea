package org.menesty.ikea.ui.pages.wizard.order.step.component;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;

/**
 * Created by Menesty on
 * 9/12/15.
 * 00:47.
 */
public class ItemProcessingInfoLabel extends HBox {
    private ProgressIndicator progressIndicator;
    private Label itemsCountLabel = new Label();
    private Label currentIndexItemLabel;
    private Label itemNameLabel;
    private ChangeListener<? super Number> changeListener;

    public ItemProcessingInfoLabel(String mailLabel) {
        Label textLabel = new Label(mailLabel);
        itemNameLabel = new Label();

        currentIndexItemLabel = new Label();

        changeListener = (observable, oldValue, newValue) -> currentIndexItemLabel.setText("   " + newValue + "");

        getChildren().addAll(textLabel, itemNameLabel, currentIndexItemLabel, itemsCountLabel, progressIndicator = new ProgressIndicator());
        hideProgress();
        currentIndexItemLabel.setText("0");
        setTotal(0);
    }

    public void setIndexProperty(IntegerProperty itemIndexProperty) {
        itemIndexProperty.removeListener(changeListener);
        itemIndexProperty.addListener(changeListener);
    }

    public void setNameProperty(StringProperty fileNameProperty) {
        itemNameLabel.textProperty().unbind();
        itemNameLabel.textProperty().bind(fileNameProperty);
    }

    public void hideProgress() {
        progressIndicator.setVisible(false);
    }

    public void showProgress() {
        progressIndicator.setVisible(true);
    }

    public void setTotal(int totalItems) {
        itemsCountLabel.setText("/" + totalItems);
    }
}
