package org.menesty.ikea.ui.controls.search;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.ui.controls.form.TextField;

public class ProductItemSearchBar extends ToolBar {

    private TextField artNumber;

    private ComboBox<ProductInfo.Group> productGroup;

    public ProductItemSearchBar() {
        InvalidationListener invalidationListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                applyFilter();
            }
        };

        artNumber = new org.menesty.ikea.ui.controls.form.TextField();
        artNumber.setDelay(1);
        artNumber.setOnDelayAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                applyFilter();
            }
        });
        artNumber.setPromptText("Product ID #");

        productGroup = new ComboBox<>();
        productGroup.getSelectionModel().selectedItemProperty().addListener(invalidationListener);
        productGroup.setPromptText("Product group");
        productGroup.getItems().add(null);
        productGroup.getItems().addAll(ProductInfo.Group.values());

    }

    private void applyFilter() {
        onSearch(collectData());
    }

    private ProductItemSearchData collectData() {
        ProductItemSearchData data = new ProductItemSearchData();
        data.artNumber = artNumber.getText();
        data.productGroup = productGroup.getSelectionModel().getSelectedItem();
        return data;
    }

    public void onSearch(ProductItemSearchData data) {

    }
}
