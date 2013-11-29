package org.menesty.ikea.ui.controls.search;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
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

        setOrientation(Orientation.VERTICAL);

        HBox checkBoxPane = null;
        int index = 0;

        for (ProductInfo.Group group : ProductInfo.Group.values()) {
            if (index == 0) {
                checkBoxPane = new HBox();
                getItems().add(checkBoxPane);
            }

            CheckBox checkBox = new CheckBox(group.name());
            checkBox.setPrefWidth(80);
            HBox.setMargin(checkBox, new Insets(0, 5, 0, 5));
            checkBox.selectedProperty().addListener(invalidationListener);
            checkBox.setUserData(group);
            productGroups.add(checkBox);
            checkBoxPane.getChildren().add(checkBox);

            index++;

            if (index == 6) index = 0;
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
