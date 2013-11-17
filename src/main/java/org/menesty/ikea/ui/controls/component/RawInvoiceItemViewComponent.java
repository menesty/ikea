package org.menesty.ikea.ui.controls.component;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.table.RawInvoiceTableView;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class RawInvoiceItemViewComponent extends BorderPane {

    private final RawInvoiceTableView rawInvoiceItemTableView;
    private final TotalStatusPanel totalStatusPanel;

    public RawInvoiceItemViewComponent(final Stage stage) {
        ToolBar rawInvoiceControl = new ToolBar();

        ImageView imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/epp-32x32.png"));
        Button exportEppButton = new Button("", imageView);
        exportEppButton.setContentDisplay(ContentDisplay.RIGHT);
        exportEppButton.setTooltip(new Tooltip("Export to EPP"));
        exportEppButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                List<RawInvoiceProductItem> items = rawInvoiceItemTableView.getItems();
                exportToEpp(prepareData(items), getTotalPrice(items));
            }
        });

        imageView = new ImageView(new Image("/styles/images/icon/xls-32x32.png"));
        Button export = new Button("", imageView);
        export.setContentDisplay(ContentDisplay.RIGHT);
        export.setTooltip(new Tooltip("Export to XLS"));
        export.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                //Set extension filter
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Xls (*.xlsx)", "*.xlsx");
                fileChooser.getExtensionFilters().add(extFilter);
                //Show save file dialog
                File file = fileChooser.showSaveDialog(stage);

                if (file != null)
                    onExport(rawInvoiceItemTableView.getItems(), file.getAbsolutePath());
            }
        });


        rawInvoiceControl.getItems().addAll(exportEppButton, export);

        rawInvoiceItemTableView = new RawInvoiceTableView() {
            @Override
            public void onRowDoubleClick(final TableRow<RawInvoiceProductItem> row) {
                RawInvoiceItemViewComponent.this.onRowDoubleClick(row);

            }
        };


        setTop(rawInvoiceControl);
        setCenter(rawInvoiceItemTableView);
        setBottom(totalStatusPanel = new TotalStatusPanel());
    }

    public abstract void onExport(List<RawInvoiceProductItem> items, String path);

    private BigDecimal getTotalPrice(List<RawInvoiceProductItem> items) {
        BigDecimal price = BigDecimal.ZERO;

        for (RawInvoiceProductItem item : items)
            price = price.add(BigDecimal.valueOf(item.getTotal()));

        return price;

    }

    public void setItems(final List<RawInvoiceProductItem> items) {
        rawInvoiceItemTableView.setItems(FXCollections.observableArrayList(items));
        totalStatusPanel.setTotal(getTotalPrice(items).doubleValue());
    }

    public abstract void onRowDoubleClick(final TableRow<RawInvoiceProductItem> row);

    public abstract void exportToEpp(List<InvoiceItem> items, BigDecimal price);

    private List<InvoiceItem> prepareData(List<RawInvoiceProductItem> items) {
        List<InvoiceItem> result = new ArrayList<>();
        List<RawInvoiceProductItem> filtered = new ArrayList<>();

        for (RawInvoiceProductItem item : items)
            if (item.getProductInfo().getPackageInfo().getWeight() > 3000 || item.getPrice() * item.getCount() > 200)
                result.addAll(InvoiceItem.get(item.getProductInfo(), item.getCount()));
            else
                filtered.add(item);

        BigDecimal price = BigDecimal.ZERO;
        int index = 1;
        BigDecimal count = BigDecimal.ZERO;

        for (RawInvoiceProductItem item : filtered) {
            BigDecimal artPrice = BigDecimal.valueOf(item.getTotal());

            if ((price.add(artPrice).doubleValue()) > 460) {
                result.add(createZestav(index, price.doubleValue(), count.doubleValue()));

                index++;
                price = artPrice;
                count = BigDecimal.valueOf(item.getCount());
            } else {
                price = price.add(artPrice);
                count = count.add(BigDecimal.valueOf(item.getCount()));
            }
        }

        if (price.doubleValue() != 0)
            result.add(createZestav(index, price.doubleValue(), count.doubleValue()));

        return result;

    }

    private InvoiceItem createZestav(int index, double price, double count) {
        String name = String.format("zestav %1$s parts %2$s", index, (int) count);
        return InvoiceItem.get("zestav " + index, name, name, price, 23, 1, 1, 1, 1).setZestav(true);
    }

}
