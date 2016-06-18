package org.menesty.ikea.ui.pages.ikea.order.dialog.invoice;

import javafx.stage.Stage;
import org.menesty.ikea.ui.controls.form.FileListField;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;

/**
 * Created by Menesty on
 * 10/5/15.
 * 18:05.
 */
public class UploadStep1 extends BaseWizardStep<IkeaInvoiceUploadDialog.IkeaInvoiceUploadInfo> {

    private FileListField fileListField;

    public UploadStep1(Stage stage) {
        fileListField = new FileListField("", stage);
        fileListField.setAllowBlank(false);
        setContent(fileListField);
    }

    @Override
    public boolean isValid() {
        return fileListField.isValid();
    }

    @Override
    public boolean canSkip(IkeaInvoiceUploadDialog.IkeaInvoiceUploadInfo param) {
        return false;
    }

    @Override
    public void collect(IkeaInvoiceUploadDialog.IkeaInvoiceUploadInfo param) {
        param.setFiles(fileListField.getValues());
    }

    @Override
    public void onActive(IkeaInvoiceUploadDialog.IkeaInvoiceUploadInfo param) {
        fileListField.setValues(param.getFiles());
    }
}
