package org.menesty.ikea.ui.pages;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.menesty.ikea.ui.controls.component.ParagonViewComponent;
import org.menesty.ikea.ui.controls.component.WarehouseViewComponent;

public class WarehousePage extends BasePage {

    private WarehouseViewComponent warehouseViewComponent;
    private ParagonViewComponent paragonViewComponent;

    public WarehousePage() {
    }

    @Override
    public void onActive(Object... params) {
        warehouseViewComponent.bindLoading(loadingPane);
        warehouseViewComponent.load();

        paragonViewComponent.bindLoading(loadingPane);
        paragonViewComponent.load();
    }

    @Override
    protected Node createView() {
        warehouseViewComponent = new WarehouseViewComponent(getDialogSupport());
        paragonViewComponent = new ParagonViewComponent(getDialogSupport());

        TabPane tabPane = new TabPane();

        {
            Tab tab = new Tab("Warehouse");
            tab.setClosable(false);
            tab.setContent(warehouseViewComponent);
            tabPane.getTabs().add(tab);
        }

        {
            Tab tab = new Tab("Paragons");
            tab.setClosable(false);
            tab.setContent(paragonViewComponent);
            tabPane.getTabs().add(tab);
        }

        return wrap(tabPane);
    }
}