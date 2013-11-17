package org.menesty.ikea.ui.controls.component;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.table.InvoicePdfTableView;
import org.menesty.ikea.ui.pages.DialogCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class InvoicePdfViewComponent extends BorderPane {

    private final Button deleteBtn;

    private InvoicePdfTableView invoicePdfTableView;

    private TotalStatusPanel statusPanel;

    public InvoicePdfViewComponent(final Stage stage) {

        invoicePdfTableView = new InvoicePdfTableView() {
            @Override
            public void onSave(InvoicePdf invoicePdf) {
                InvoicePdfViewComponent.this.onSave(invoicePdf);
            }

            @Override
            public void onCheck(InvoicePdf invoicePdf, boolean newValue) {
                deleteBtn.setDisable(getChecked().size() == 0);
            }
        };

        invoicePdfTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        invoicePdfTableView.getSelectionModel().selectedIndexProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                onSelect(getSelected());
            }
        });

        ToolBar pdfToolBar = new ToolBar();
        ImageView imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/pdf-32x32.png"));
        Button uploadInvoice = new Button("", imageView);
        uploadInvoice.setContentDisplay(ContentDisplay.RIGHT);
        uploadInvoice.setTooltip(new Tooltip("Upload Invoice PDF"));
        uploadInvoice.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Invoice PDF location");
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Pdf files (*.pdf)", "*.pdf");
                fileChooser.getExtensionFilters().add(extFilter);
                File selectedFile = fileChooser.showOpenDialog(stage);

                if (selectedFile != null)
                    onExport(selectedFile.getName(), selectedFile.getPath());

            }
        });

        imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/delete-32x32.png"));
        deleteBtn = new Button(null, imageView);
        deleteBtn.setContentDisplay(ContentDisplay.RIGHT);
        deleteBtn.setTooltip(new Tooltip("Delete invoice"));
        deleteBtn.setDisable(true);
        deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Dialog.confirm("Are you sure to delete items", new DialogCallback() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onYes() {
                    }
                });
            }
        });

        pdfToolBar.getItems().addAll(uploadInvoice, deleteBtn);

        setTop(pdfToolBar);
        setCenter(invoicePdfTableView);
        setBottom(statusPanel = new TotalStatusPanel());
    }

    public abstract void onSave(InvoicePdf invoicePdf);

    public abstract void onExport(String fileName, String filePath);

    // public abstract void onCheck(InvoicePdf invoicePdf);

    public abstract void onSelect(List<InvoicePdf> invoicePdfs);

    public void setItems(List<InvoicePdf> items) {
        invoicePdfTableView.setItems(transform(items));

        double total = 0d;
        for (InvoicePdf item : items)
            total += item.getPrice();

        statusPanel.setTotal(total);
    }

    private List<InvoicePdf> revertTransform(List<InvoicePdfTableView.InvoicePdfTableItem> entities) {
        List<InvoicePdf> result = new ArrayList<>();

        for (InvoicePdfTableView.InvoicePdfTableItem item : entities)
            result.add(item.getInvoicePdf());

        return result;
    }

    private ObservableList<InvoicePdfTableView.InvoicePdfTableItem> transform(List<InvoicePdf> entities) {
        ObservableList<InvoicePdfTableView.InvoicePdfTableItem> result = FXCollections.observableArrayList();

        if (entities == null) return result;

        for (InvoicePdf entity : entities)
            result.add(invoicePdfTableView.new InvoicePdfTableItem(false, entity));

        return result;

    }

    public List<InvoicePdf> getChecked() {
        List<InvoicePdf> checked = new ArrayList<>();

        for (InvoicePdfTableView.InvoicePdfTableItem item : invoicePdfTableView.getItems())
            if (item.checkedProperty().getValue())
                checked.add(item.getInvoicePdf());

        return checked;
    }

    public List<InvoicePdf> getSelected() {
        List<InvoicePdfTableView.InvoicePdfTableItem> selected = invoicePdfTableView.getSelectionModel().getSelectedItems();
        return revertTransform(selected);
    }

}
