package org.menesty.ikea.ui.controls.search;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import org.menesty.ikea.domain.ProductInfo;

public class StorageLackSearchBar extends ToolBar {

    private ComboBox<ProductInfo.Group> productGroup;

    public StorageLackSearchBar() {
        InvalidationListener invalidationListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                applyFilter();
            }
        };

        productGroup = new ComboBox<>();
        productGroup.getSelectionModel().selectedItemProperty().addListener(invalidationListener);
        productGroup.setPromptText("Product group");
        productGroup.getItems().add(null);
        productGroup.getItems().addAll(ProductInfo.Group.values());

        getItems().addAll(productGroup);
    }

    private void applyFilter() {
        onSearch(collectData());
    }

    private StorageLackSearchData collectData() {
        StorageLackSearchData data = new StorageLackSearchData();
        data.productGroup = productGroup.getSelectionModel().getSelectedItem();
        return data;
    }

    public void onSearch(StorageLackSearchData data) {

    }
}
