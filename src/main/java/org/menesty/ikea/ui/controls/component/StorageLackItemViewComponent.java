package org.menesty.ikea.ui.controls.component;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.menesty.ikea.domain.StorageLack;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.search.StorageLackSearchBar;
import org.menesty.ikea.ui.controls.search.StorageLackSearchData;
import org.menesty.ikea.ui.controls.table.StorageLackItemTableView;
import org.menesty.ikea.util.NumberUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class StorageLackItemViewComponent extends BorderPane {

    private final Button exportToIkeaBtn;

    private StorageLackItemTableView storageLackItemTableView;

    private List<StorageLack> items;

    private StatusPanel statusPanel;

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
                updateTotals();
            }
        };

        VBox toolBarBox = new VBox();

        toolBarBox.getChildren().addAll(toolBar, searchBar);

        setTop(toolBarBox);
        setCenter(storageLackItemTableView = new StorageLackItemTableView());

        setBottom(statusPanel = new StatusPanel());
    }

    public void setItems(List<StorageLack> items) {
        storageLackItemTableView.setItems(FXCollections.observableArrayList(items));
        this.items = Collections.unmodifiableList(items);
        updateTotals();
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

    private void updateTotals() {
        List<StorageLack> items = storageLackItemTableView.getItems();
        BigDecimal totalLack = BigDecimal.ZERO;
        BigDecimal totalOverbought = BigDecimal.ZERO;

        for (StorageLack item : items)
            if (item.isExist() && item.getCount() > 0)
                totalLack = totalLack.add(BigDecimal.valueOf(item.getCount()).multiply(BigDecimal.valueOf(item.getProductInfo().getPrice())));
            else
                totalOverbought = totalOverbought.add(BigDecimal.valueOf(item.getCount()).abs().multiply(BigDecimal.valueOf(item.getProductInfo().getPrice())));

        statusPanel.setTotal(totalLack.doubleValue());
        statusPanel.setOverbought(totalOverbought.doubleValue());
    }

    class StatusPanel extends TotalStatusPanel {
        private Label overbought;

        StatusPanel() {
            super("Total lack");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            getItems().addAll(spacer, new Label("Total overbought :"), overbought = new Label());
        }

        public void setOverbought(double overbought) {
            this.overbought.setText(NumberFormat.getNumberInstance().format(NumberUtil.round(overbought)));
        }
    }
}
