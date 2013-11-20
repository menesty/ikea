package org.menesty.ikea.ui.controls.search;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToolBar;
import org.menesty.ikea.domain.ProductInfo;

import java.util.ArrayList;
import java.util.List;

public class StorageLackSearchBar extends ToolBar {

    private List<CheckBox> productGroups;

    public StorageLackSearchBar() {
        InvalidationListener invalidationListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                applyFilter();
            }
        };

        productGroups = new ArrayList<>();

        for (ProductInfo.Group group : ProductInfo.Group.values()) {
            CheckBox checkBox = new CheckBox(group.name());
            checkBox.selectedProperty().addListener(invalidationListener);
            checkBox.setUserData(group);
            productGroups.add(checkBox);
            getItems().add(checkBox);
        }
    }

    private void applyFilter() {
        onSearch(collectData());
    }

    private StorageLackSearchData collectData() {
        StorageLackSearchData data = new StorageLackSearchData();
        List<ProductInfo.Group> groups = new ArrayList<>();

        for (CheckBox checkBox : productGroups)
            if (checkBox.isSelected())
                groups.add((ProductInfo.Group) checkBox.getUserData());

        data.productGroup = groups;
        return data;
    }

    public void onSearch(StorageLackSearchData data) {

    }
}
