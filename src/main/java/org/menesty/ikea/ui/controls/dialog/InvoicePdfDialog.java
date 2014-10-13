package org.menesty.ikea.ui.controls.dialog;

import javafx.stage.Stage;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.ui.controls.form.DoubleTextField;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.pages.EntityDialogCallback;

public class InvoicePdfDialog extends BaseDialog {
    private InvoiceForm form;

    private InvoicePdf currentInvoice;

    private EntityDialogCallback<InvoicePdf> callback;

    public InvoicePdfDialog(Stage stage) {
        super(stage);
        addRow(form = new InvoiceForm(), bottomBar);
        okBtn.setText("Save");
    }

    public void bind(InvoicePdf invoiceItem, EntityDialogCallback<InvoicePdf> callback) {
        currentInvoice = invoiceItem;
        this.callback = callback;

        if (invoiceItem.getId() == null)
            setTitle("Create invoice");
        else
            setTitle("Edit invoice");

        form.reset();
        form.setPrice(invoiceItem.getPrice());
        form.setName(invoiceItem.getName());
        form.setInvoiceNumber(invoiceItem.getInvoiceNumber());

    }

    @Override
    public void onOk() {
        if (!form.isValid())
            return;

        currentInvoice.setPrice(form.getPrice());
        currentInvoice.setName(form.getName());
        currentInvoice.setInvoiceNumber(form.getInvoiceNumber());

        if (callback != null)
            callback.onSave(currentInvoice);
    }

    @Override
    public void onCancel() {
        if (callback != null)
            callback.onCancel();
    }

    private class InvoiceForm extends FormPane {
        DoubleTextField price;
        TextField name;
        TextField invoiceNumber;

        public InvoiceForm() {
            add(name = new TextField(null, "Name", false));
            add(invoiceNumber = new TextField(null, "Invoice Number", false));
            add(price = new DoubleTextField("Price"));
        }

        public void setPrice(Double price) {
            this.price.setNumber(price);
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setInvoiceNumber(String invoiceNumber) {
            this.invoiceNumber.setText(invoiceNumber);
        }

        public Double getPrice() {
            return this.price.getNumber();
        }

        public String getName() {
            return this.name.getText();
        }

        public String getInvoiceNumber() {
            return this.invoiceNumber.getText();
        }
    }

}
