package org.menesty.ikea.ui.pages.ikea.order.dialog.invoice;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.invoice.Invoice;
import org.menesty.ikea.lib.service.parse.pdf.invoice.InvoiceParseResult;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.pane.wizard.WizardPanel;
import org.menesty.ikea.ui.pages.ikea.order.component.InvoiceViewComponent;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty on
 * 10/5/15.
 * 17:57.
 */
public class IkeaInvoiceUploadDialog extends BaseDialog {


    private final WizardPanel<IkeaInvoiceUploadInfo> wizardPanel;
    private IkeaInvoiceUploadInfo ikeaInvoiceUploadInfo;
    private final ExportUploadService exportUploadService;
    private InvoiceViewComponent.InvoiceActionListener exportListener;

    public IkeaInvoiceUploadDialog(Stage stage) {
        super(stage);
        setTitle(I18n.UA.getString(I18nKeys.INVOICES_UPLOAD_PDF_DIALOG_TITLE));
        setAllowAutoHide(false);

        LoadingPane loadingPane = new LoadingPane();
        exportUploadService = new ExportUploadService();

        loadingPane.bindTask(exportUploadService);

        wizardPanel = new WizardPanel<>(ikeaInvoiceUploadInfo = new IkeaInvoiceUploadInfo());
        wizardPanel.addStep(new UploadStep1(stage));
        wizardPanel.addStep(new UploadStep2());
        wizardPanel.setOnFinishListener(param -> {
            exportUploadService.setUploadInfo(param);
            exportUploadService.restart();
        });

        exportUploadService.setOnSucceededListener(value -> {
            wizardPanel.unLockButtons();
            exportListener.onExport(value);
            onActionOk();
        });

        Button button = new Button(I18n.UA.getString(I18nKeys.CLOSE));
        button.setOnAction(event -> onActionOk());
        wizardPanel.addButton(button);

        setMaxSize(640, 600);
        cancelBtn.setVisible(false);
        okBtn.setVisible(false);

        StackPane mainPane = new StackPane();
        VBox.setVgrow(mainPane, Priority.ALWAYS);

        mainPane.getChildren().addAll(wizardPanel, loadingPane);

        addRow(mainPane);
    }

    @Override
    public void onShow() {
        wizardPanel.start();
    }

    public void bind(Long ikeaProcessId, InvoiceViewComponent.InvoiceActionListener exportListener) {
        ikeaInvoiceUploadInfo.reset();
        ikeaInvoiceUploadInfo.setIkeaProcessOrderId(ikeaProcessId);
        this.exportListener = exportListener;
    }

    class IkeaInvoiceUploadInfo {
        private List<File> files = new ArrayList<>();
        private ArrayList<InvoiceParseResult> invoiceParseResult;
        private Long ikeaProcessOrderId;

        public Long getIkeaProcessOrderId() {
            return ikeaProcessOrderId;
        }

        public void setIkeaProcessOrderId(Long ikeaProcessOrderId) {
            this.ikeaProcessOrderId = ikeaProcessOrderId;
        }

        public List<File> getFiles() {
            return files;
        }

        public void setFiles(List<File> files) {
            this.files = files;
        }

        public void setInvoiceParseResult(ArrayList<InvoiceParseResult> invoiceParseResult) {
            this.invoiceParseResult = invoiceParseResult;
        }

        public ArrayList<InvoiceParseResult> getInvoiceParseResult() {
            return invoiceParseResult;
        }

        public void reset() {
            files.clear();
            invoiceParseResult = null;
            ikeaProcessOrderId = null;
        }
    }


    class ExportUploadService extends AbstractAsyncService<List<Invoice>> {
        private ObjectProperty<IkeaInvoiceUploadInfo> uploadInfoObjectProperty = new SimpleObjectProperty<>();

        @Override
        protected Task<List<Invoice>> createTask() {
            final IkeaInvoiceUploadInfo _uploadInfo = uploadInfoObjectProperty.get();
            return new Task<List<Invoice>>() {
                @Override
                protected List<Invoice> call() throws Exception {
                    APIRequest apiRequest = HttpServiceUtil.get("/ikea-order/invoice/add/" + _uploadInfo.getIkeaProcessOrderId());
                    return apiRequest.postData(_uploadInfo.getInvoiceParseResult(), new TypeReference<List<Invoice>>() {
                    });
                }
            };
        }

        public void setUploadInfo(IkeaInvoiceUploadInfo uploadInfo) {
            uploadInfoObjectProperty.set(uploadInfo);
        }
    }
}
