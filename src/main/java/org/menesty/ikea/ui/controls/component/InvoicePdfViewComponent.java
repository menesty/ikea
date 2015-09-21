package org.menesty.ikea.ui.controls.component;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.db.DatabaseService;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.dialog.InvoicePdfDialog;
import org.menesty.ikea.ui.controls.table.InvoicePdfTableView;
import org.menesty.ikea.ui.pages.DialogCallback;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.util.FileChooserUtil;
import org.menesty.ikea.util.NumberUtil;
import org.menesty.ikea.util.ToolTipUtil;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class InvoicePdfViewComponent extends BorderPane {

    private final Button deleteBtn;

    private InvoicePdfTableView invoicePdfTableView;

    private WeightStatusPanel statusPanel;

    private Button syncBtn;

    //private Button fakePdf;

    private InvoicePdfDialog invoicePdfDialog;

    public InvoicePdfViewComponent(final DialogSupport dialogSupport) {
        invoicePdfDialog = new InvoicePdfDialog(dialogSupport.getStage());

        invoicePdfTableView = new InvoicePdfTableView() {
            @Override
            public void onUpload(InvoicePdf invoicePdf) {
                InvoicePdfViewComponent.this.onUpload(invoicePdf);
            }

            @Override
            public void onCheck(InvoicePdf invoicePdf, boolean newValue) {
                deleteBtn.setDisable(getChecked().size() == 0);
            }

            @Override
            protected void onRowDoubleClick(TableRow<InvoicePdfTableItem> row) {
                if (row.getItem() != null) {
                    invoicePdfDialog.bind(row.getItem().getInvoicePdf(), new EntityDialogCallback<InvoicePdf>() {
                        @Override
                        public void onSave(InvoicePdf invoicePdf, Object... params) {
                            InvoicePdfViewComponent.this.onSave(invoicePdf);
                            dialogSupport.hidePopupDialog();
                        }

                        @Override
                        public void onCancel() {
                            dialogSupport.hidePopupDialog();
                        }
                    });
                }
            }
        };

        invoicePdfTableView.getSelectionModel().selectedIndexProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                onSelect(getSelected());
            }
        });

        ToolBar pdfToolBar = new ToolBar();

        {
            Button button = new Button(null, ImageFactory.createAdd32Icon());
            button.setTooltip(new Tooltip("Create Invoice"));
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    showAddEditDialog(dialogSupport, new InvoicePdf(getCustomerOrder()));
                }
            });

            pdfToolBar.getItems().add(button);
        }

        Button uploadInvoice = new Button(null, ImageFactory.createPdf32Icon());
        uploadInvoice.setTooltip(new Tooltip("Upload Invoice PDF"));
        uploadInvoice.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                FileChooser fileChooser = FileChooserUtil.getPdf();

                List<File> selectedFile = fileChooser.showOpenMultipleDialog(dialogSupport.getStage());

                if (selectedFile != null && selectedFile.size() > 0)
                    onImport(filter(selectedFile));

            }
        });

        deleteBtn = new Button(null, ImageFactory.createDelete32Icon());
        deleteBtn.setTooltip(ToolTipUtil.create("Delete invoice"));
        deleteBtn.setDisable(true);
        deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Dialog.confirm(dialogSupport, "Are you sure to delete items", new DialogCallback() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onYes() {
                        onDelete(getChecked());
                    }
                });
            }
        });



        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        syncBtn = new Button(null, ImageFactory.createSync32Icon());
        syncBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Dialog.confirm(dialogSupport, "Warning", "Remote warehouse will be updated with new data.", new DialogCallback() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onYes() {
                        onSync(true);
                    }
                });
            }
        });
        syncBtn.setDisable(true);

        Button fakePdf = new Button(null, ImageFactory.createBalance32Icon());
        fakePdf.setTooltip(new Tooltip("Fake pdf"));
        fakePdf.setOnAction(actionEvent -> Dialog.confirm(dialogSupport, "Are you sure to create Fake items", new DialogCallback() {
            @Override
            public void onCancel() {
            }

            @Override
            public void onYes() {
                onFake();
            }
        }));


        pdfToolBar.getItems().addAll(uploadInvoice, deleteBtn, spacer, syncBtn, fakePdf);

        setTop(pdfToolBar);
        setCenter(invoicePdfTableView);
        setBottom(statusPanel = new WeightStatusPanel());
    }

    protected abstract void onFake();

    private void showAddEditDialog(final DialogSupport dialogSupport, InvoicePdf invoicePdf) {
        invoicePdfDialog.bind(invoicePdf, new EntityDialogCallback<InvoicePdf>() {
            @Override
            public void onSave(InvoicePdf invoicePdf, Object... params) {
                InvoicePdfViewComponent.this.onSave(invoicePdf);
                dialogSupport.hidePopupDialog();
            }

            @Override
            public void onCancel() {
                dialogSupport.hidePopupDialog();
            }
        });

        dialogSupport.showPopupDialog(invoicePdfDialog);
    }

    protected abstract void onSync(boolean clear);

    protected abstract CustomerOrder getCustomerOrder();

    private List<File> filter(List<File> files) {
        List<File> result = new ArrayList<>(files);
        Iterator<File> iter = result.iterator();

        while (iter.hasNext()) {
            File file = iter.next();

            for (InvoicePdfTableView.InvoicePdfTableItem item : invoicePdfTableView.getItems())
                if (item.getInvoicePdf().getName().equals(file.getName()))
                    iter.remove();
        }
        return result;
    }

    public abstract void onDelete(List<InvoicePdf> items);

    public abstract void onSave(InvoicePdf invoicePdf);

    public abstract void onImport(List<File> files);

    public abstract void onUpload(InvoicePdf invoicePdf);

    public abstract void onSelect(InvoicePdf invoicePdf);

    public void setItems(List<InvoicePdf> items) {
        invoicePdfTableView.setItems(transform(items));
        updateState();
    }

    public void update(InvoicePdf invoicePdf) {
        invoicePdfTableView.update(transform(invoicePdf));
    }

    private ObservableList<InvoicePdfTableView.InvoicePdfTableItem> transform(List<InvoicePdf> entities) {
        ObservableList<InvoicePdfTableView.InvoicePdfTableItem> result = FXCollections.observableArrayList();

        if (entities == null) return result;

        for (InvoicePdf entity : entities)
            result.add(transform(entity));

        return result;

    }

    private InvoicePdfTableView.InvoicePdfTableItem transform(InvoicePdf entity) {
        return invoicePdfTableView.new InvoicePdfTableItem(false, entity);
    }

    public List<InvoicePdf> getChecked() {
        List<InvoicePdf> checked = new ArrayList<>();

        for (InvoicePdfTableView.InvoicePdfTableItem item : invoicePdfTableView.getItems())
            if (item.checkedProperty().getValue())
                checked.add(item.getInvoicePdf());

        return checked;
    }

    public InvoicePdf getSelected() {
        return invoicePdfTableView.getSelectionModel().getSelectedItem() != null ?
                invoicePdfTableView.getSelectionModel().getSelectedItem().getInvoicePdf() : null;
    }

    public void updateState() {
        DatabaseService.runInTransaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                boolean allowSync = true;
                double total = 0d;
                double weight = 0d;

                for (InvoicePdfTableView.InvoicePdfTableItem item : invoicePdfTableView.getItems()) {
                    total += item.getInvoicePdf().getPrice();

                    for (RawInvoiceProductItem rawItem : item.getInvoicePdf().getProducts()) {
                        ProductInfo productInfo = rawItem.getProductInfo();

                        if (productInfo != null)
                            weight += rawItem.getCount() * productInfo.getPackageInfo().getWeight();

                    }

                    boolean hasItems = ServiceFacade.getInvoiceItemService().hasItems(item.getInvoicePdf());
                    boolean update = item.hasItems() != null && item.hasItems() != hasItems;

                    item.setItems(hasItems);

                    if (update)
                        invoicePdfTableView.update(item);

                    if (allowSync && !hasItems)
                        allowSync = false;
                }

                syncBtn.setDisable(!allowSync);
                statusPanel.setTotal(total);

                statusPanel.setWeight(weight);

                return null;
            }
        });
    }

    public Button getSyncBtn() {
        return syncBtn;
    }
}

class WeightStatusPanel extends TotalStatusPanel {
    private Label weightLabel;

    public WeightStatusPanel() {
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        getItems().addAll(space, new Label("Weight :"), weightLabel = new Label());
    }

    public void setWeight(double weight) {
        //weightLabel.setText(NumberFormat.getNumberInstance().format(NumberUtil.convertToKg(weight)));
    }
}