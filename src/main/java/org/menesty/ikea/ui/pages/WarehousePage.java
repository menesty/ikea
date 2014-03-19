package org.menesty.ikea.ui.pages;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.ui.controls.component.ParagonViewComponent;
import org.menesty.ikea.ui.controls.component.WarehouseViewComponent;

public class WarehousePage extends BasePage {

    private WarehouseViewComponent warehouseViewComponent;
    private ParagonViewComponent paragonViewComponent;

    public WarehousePage() {
        super("Warehouse");

    }

    @Override
    public void onActive(Object... params) {
        warehouseViewComponent.bindLoading(loadingPane);
        warehouseViewComponent.load();

        paragonViewComponent.bindLoading(loadingPane);
        paragonViewComponent.load();
    }

    @Override
    public Node createView() {
        warehouseViewComponent = new  WarehouseViewComponent();
        paragonViewComponent = new ParagonViewComponent();

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

        StackPane pane = createRoot();
        pane.getChildren().add(0, tabPane);

        return pane;
    }



    @Override
    protected Node createIconContent() {
        return ImageFactory.createWarehouseIcon72();
    }
}