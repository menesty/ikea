package org.menesty.ikea.ui.controls.component;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.table.RawInvoiceTableView;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public abstract class RawInvoiceItemViewComponent extends BorderPane {

    private final RawInvoiceTableView rawInvoiceItemTableView;
    private final TotalStatusPanel totalStatusPanel;

    private String artPrefix = "";

    public RawInvoiceItemViewComponent(final Stage stage) {
        ToolBar rawInvoiceControl = new ToolBar();


        Button export = new Button(null, ImageFactory.createXlsExportIcon());
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


        rawInvoiceControl.getItems().add(export);

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

    public void setEppPrefix(String artPrefix) {
        this.artPrefix = artPrefix;
    }

}
