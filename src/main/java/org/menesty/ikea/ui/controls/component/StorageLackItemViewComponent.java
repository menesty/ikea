package org.menesty.ikea.ui.controls.component;

import javafx.collections.FXCollections;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.domain.StorageLack;
import org.menesty.ikea.ui.controls.table.StorageLackItemTableView;

import java.util.List;

public class StorageLackItemViewComponent extends BorderPane {
    private StorageLackItemTableView storageLackItemTableView;

    public StorageLackItemViewComponent() {
        setCenter(storageLackItemTableView = new StorageLackItemTableView());
    }

    public void setItems(List<StorageLack> items) {
        storageLackItemTableView.setItems(FXCollections.observableArrayList(items));
    }
}
