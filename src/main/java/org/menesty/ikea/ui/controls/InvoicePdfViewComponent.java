package org.menesty.ikea.ui.controls;

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
import org.menesty.ikea.ui.controls.table.InvoicePdfTableView;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class InvoicePdfViewComponent extends BorderPane {

    private InvoicePdfTableView invoicePdfTableView;

    private StatusPanel statusPanel;

    public InvoicePdfViewComponent(final Stage stage) {

        invoicePdfTableView = new InvoicePdfTableView() {
            @Override
            public void onSave(InvoicePdf invoicePdf) {
                InvoicePdfViewComponent.this.onSave(invoicePdf);
            }

            @Override
            public void onCheck(InvoicePdf invoicePdf) {
                InvoicePdfViewComponent.this.onCheck(invoicePdf);
            }
        };

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

        pdfToolBar.getItems().add(uploadInvoice);

        setTop(pdfToolBar);
        setCenter(invoicePdfTableView);
        setBottom(statusPanel = new StatusPanel());
    }

    public abstract void onSave(InvoicePdf invoicePdf);

    public abstract void onExport(String fileName, String filePath);

    public abstract void onCheck(InvoicePdf invoicePdf);

    public void setItems(List<InvoicePdf> items) {
        invoicePdfTableView.setItems(transform(items));

        double total = 0d;
        for (InvoicePdf item : items)
            total += item.getPrice();

        statusPanel.setTotal(total);
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

    class StatusPanel extends ToolBar {
        private Label totalLabel;

        public StatusPanel() {
            getItems().add(new Label("Total :"));
            getItems().add(totalLabel = new Label());

        }

        public void setTotal(double total) {
            double totalPrice = BigDecimal.valueOf(total).setScale(2, RoundingMode.CEILING).doubleValue();
            totalLabel.setText(NumberFormat.getNumberInstance().format(totalPrice));
        }
    }
}
