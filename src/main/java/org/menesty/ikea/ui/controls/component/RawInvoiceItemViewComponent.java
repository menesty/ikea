package org.menesty.ikea.ui.controls.component;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableRow;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.menesty.ikea.IkeaApplication;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.RawInvoiceItemDialog;
import org.menesty.ikea.ui.controls.table.RawInvoiceTableView;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.util.FileChooserUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public abstract class RawInvoiceItemViewComponent extends BorderPane {

    private final RawInvoiceTableView rawInvoiceItemTableView;

    private final TotalStatusPanel totalStatusPanel;

    private String artPrefix = "";

    private RawInvoiceItemDialog rawInvoiceItemDialog;

    private ToolBar rawInvoiceControl;


    public RawInvoiceItemViewComponent(final Stage stage) {
        rawInvoiceItemDialog = new RawInvoiceItemDialog();

         rawInvoiceControl = new ToolBar();
        {
            Button createRawInvoice = new Button(null, ImageFactory.createAdd32Icon());
            createRawInvoice.setTooltip(new Tooltip("Create Invoice Item"));
            createRawInvoice.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    InvoicePdf invoicePdf = getInvoicePdf();

                    if (invoicePdf == null)
                        return;

                    rawInvoiceItemDialog.bind(new RawInvoiceProductItem(invoicePdf), new EntityDialogCallback<RawInvoiceProductItem>() {
                        @Override
                        public void onSave(RawInvoiceProductItem item, Object... params) {
                            RawInvoiceItemViewComponent.this.onSave(item);
                            IkeaApplication.get().hidePopupDialog();
                        }

                        @Override
                        public void onCancel() {
                            IkeaApplication.get().hidePopupDialog();
                        }
                    });

                    IkeaApplication.get().showPopupDialog(rawInvoiceItemDialog);
                }
            });
            rawInvoiceControl.getItems().add(createRawInvoice);
        }
        {
            Button export = new Button(null, ImageFactory.createXlsExportIcon());
            export.setTooltip(new Tooltip("Export to XLS"));
            export.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    FileChooser fileChooser = FileChooserUtil.getXls();
                    //Show save file dialog
                    File file = fileChooser.showSaveDialog(stage);

                    if (file != null)
                        onExport(rawInvoiceItemTableView.getItems(), file.getAbsolutePath());
                }
            });
            rawInvoiceControl.getItems().add(export);
        }

        disableControls(true);

        {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            rawInvoiceControl.getItems().add(spacer);
        }

        {
            Button button = new Button(null, ImageFactory.createEppExport32Icon());
            button.setTooltip(new Tooltip("Export All Invoice Items to EPP"));
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    FileChooser fileChooser = FileChooserUtil.getEpp();
                    fileChooser.setInitialFileName(getOrderName().replaceAll("[/-]", "_") + ".epp");

                    File selectedFile = fileChooser.showSaveDialog(stage);

                    if (selectedFile != null) {
                        exportEpp(selectedFile.getAbsolutePath());
                        FileChooserUtil.setDefaultDir(selectedFile);
                    }
                }
            });
            rawInvoiceControl.getItems().add(button);
        }


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

    protected abstract String getOrderName();

    protected abstract void exportEpp(String filePath);

    protected abstract void onSave(RawInvoiceProductItem item);

    protected abstract InvoicePdf getInvoicePdf();

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


    public void disableControls(boolean disable) {
        rawInvoiceControl.setDisable(disable);
    }
}
