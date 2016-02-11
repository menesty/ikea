package org.menesty.ikea.ui.pages.ikea.order.dialog.invoice;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.service.parse.pdf.invoice.RawInvoiceItem;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;
import org.menesty.ikea.ui.pages.wizard.order.step.component.ItemProcessingInfoLabel;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;

import java.util.ArrayList;

/**
 * Created by Menesty on
 * 10/5/15.
 * 18:06.
 */
public class UploadStep2 extends BaseWizardStep<IkeaInvoiceUploadDialog.IkeaInvoiceUploadInfo> {
    private InvoiceInformationTableView invoiceInformationTableView;
    private RawInvoiceItemTableView rawInvoiceItemTableView;
    private LoadInvoiceParseService loadService;

    public UploadStep2() {
        VBox mainPane = new VBox(5);

        ItemProcessingInfoLabel progress = new ItemProcessingInfoLabel(I18n.UA.getString(I18nKeys.INVOICE_PARSE_FILE_INFO));
        invoiceInformationTableView = new InvoiceInformationTableView();
        invoiceInformationTableView.setMinHeight(150);

        invoiceInformationTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            rawInvoiceItemTableView.getItems().clear();

            if (newValue != null) {
                rawInvoiceItemTableView.getItems().addAll(newValue.getRawInvoiceItems());
            }
        });

        rawInvoiceItemTableView = new RawInvoiceItemTableView();
        VBox.setVgrow(rawInvoiceItemTableView, Priority.ALWAYS);

        mainPane.getChildren().addAll(progress, invoiceInformationTableView, rawInvoiceItemTableView);

        loadService = new LoadInvoiceParseService(parseResult ->{
            invoiceInformationTableView.getItems().add(parseResult);
        }, progress);
        loadService.setOnSucceeded(event -> getWizardPanel().unLockButtons());

        setContent(mainPane);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean canSkip(IkeaInvoiceUploadDialog.IkeaInvoiceUploadInfo param) {
        return false;
    }

    @Override
    public void collect(IkeaInvoiceUploadDialog.IkeaInvoiceUploadInfo param) {
        param.setInvoiceParseResult(new ArrayList<>(invoiceInformationTableView.getItems()));
    }

    @Override
    public void onActive(IkeaInvoiceUploadDialog.IkeaInvoiceUploadInfo param) {
        getWizardPanel().lockButtons();
        invoiceInformationTableView.getItems().clear();
        rawInvoiceItemTableView.getItems().clear();

        loadService.setFiles(param.getFiles());
        loadService.restart();
    }
}


