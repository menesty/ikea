package org.menesty.ikea.ui.controls.search;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ToolBar;
import org.menesty.ikea.ui.controls.form.DoubleTextField;

public class WarehouseSearchBar extends ToolBar {
    private DoubleTextField price;

    public WarehouseSearchBar() {
        price = new DoubleTextField();
        price.setDelay(1);
        price.setOnDelayAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                applyFilter();
            }
        });
        price.setPromptText("Price <=");
        getItems().add(price);
    }

    private WarehouseItemSearchData collectData() {
        WarehouseItemSearchData data = new WarehouseItemSearchData();
        data.price = this.price.getNumber();
        return data;
    }

    private void applyFilter() {
        onSearch(collectData());
    }

    public void onSearch(WarehouseItemSearchData data) {

    }

}
