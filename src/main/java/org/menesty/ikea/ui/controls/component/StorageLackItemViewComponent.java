package org.menesty.ikea.ui.controls.component;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.menesty.ikea.domain.StorageLack;
import org.menesty.ikea.ui.controls.search.StorageLackSearchBar;
import org.menesty.ikea.ui.controls.search.StorageLackSearchData;
import org.menesty.ikea.ui.controls.table.StorageLackItemTableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class StorageLackItemViewComponent extends BorderPane {

    private final Button exportToIkeaBtn;

    private StorageLackItemTableView storageLackItemTableView;

    private List<StorageLack> items;

    public StorageLackItemViewComponent() {
        ToolBar toolBar = new ToolBar();

        exportToIkeaBtn = new Button(null, new ImageView(new Image("/styles/images/icon/ikea-32x32.png")));

        exportToIkeaBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                onExportToIkea(storageLackItemTableView.getItems());
            }
        });
        toolBar.getItems().add(exportToIkeaBtn);


        StorageLackSearchBar searchBar = new StorageLackSearchBar() {
            public void onSearch(StorageLackSearchData data) {
                storageLackItemTableView.setItems(FXCollections.observableArrayList(filter(items, data)));
            }
        };

        VBox toolBarBox = new VBox();

        toolBarBox.getChildren().addAll(toolBar, searchBar);

        setTop(toolBarBox);
        setCenter(storageLackItemTableView = new StorageLackItemTableView());
    }

    public void setItems(List<StorageLack> items) {
        storageLackItemTableView.setItems(FXCollections.observableArrayList(items));
        this.items = Collections.unmodifiableList(items);
    }

    public void disableIkeaExport(boolean disable) {
        exportToIkeaBtn.setDisable(disable);
    }

    public List<StorageLack> getItems() {
        return Collections.unmodifiableList(storageLackItemTableView.getItems());
    }

    protected abstract void onExportToIkea(List<StorageLack> items);


    private List<StorageLack> filter(List<StorageLack> items, StorageLackSearchData data) {
        if (data.productGroup == null || data.productGroup.size() == 0)
            return new ArrayList<>(items);

        List<StorageLack> result = new ArrayList<>();

        for (StorageLack item : items)
            if (data.productGroup.contains(item.getProductInfo().getGroup()))
                result.add(item);

        return result;
    }
}
