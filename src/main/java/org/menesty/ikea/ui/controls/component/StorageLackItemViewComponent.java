package org.menesty.ikea.ui.controls.component;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.domain.StorageLack;
import org.menesty.ikea.ui.controls.table.StorageLackItemTableView;

import java.util.Collections;
import java.util.List;

public abstract class StorageLackItemViewComponent extends BorderPane {

    private final Button exportToIkeaBtn;

    private StorageLackItemTableView storageLackItemTableView;

    public StorageLackItemViewComponent() {
        ToolBar toolBar = new ToolBar();

        exportToIkeaBtn = new Button(null, new ImageView(new Image("/styles/images/icon/ikea-32x32.png")));

        exportToIkeaBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                onExportToIkea();
            }
        });
        toolBar.getItems().add(exportToIkeaBtn);
        setTop(toolBar);
        setCenter(storageLackItemTableView = new StorageLackItemTableView());
    }

    public void setItems(List<StorageLack> items) {
        storageLackItemTableView.setItems(FXCollections.observableArrayList(items));
    }

    public void disableIkeaExport(boolean disable) {
        exportToIkeaBtn.setDisable(disable);
    }

    public List<StorageLack> getItems() {
        return Collections.unmodifiableList(storageLackItemTableView.getItems());
    }

    protected abstract void onExportToIkea();
}
