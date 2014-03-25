package org.menesty.ikea.ui.controls.dialog;

import javafx.scene.control.TextField;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.ui.controls.form.DoubleTextField;
import org.menesty.ikea.ui.pages.EntityDialogCallback;

public class InvoicePdfDialog extends BaseDialog {
    private InvoiceForm form;

    private InvoicePdf currentInvoice;

    private EntityDialogCallback<InvoicePdf> callback;

    public InvoicePdfDialog(){
        addRow(createTitle("Create invoice"));

        addRow(form = new InvoiceForm(), bottomBar);
        okBtn.setText("Save");
    }

    public void bind(InvoicePdf invoiceItem, EntityDialogCallback<InvoicePdf> callback) {
        currentInvoice = invoiceItem;
        this.callback = callback;

        form.reset();
        form.setPrice(invoiceItem.getPrice());
        form.setName(invoiceItem.getName());
        form.setInvoiceNumber(invoiceItem.getInvoiceNumber());

    }

    @Override
    public void onOk() {
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

    private class InvoiceForm extends RowPanel {
        DoubleTextField price;
        TextField name;
        TextField invoiceNumber;

        public InvoiceForm(){
            addRow("Name", name = new TextField());
            addRow("Invoice Number", invoiceNumber = new TextField());
            addRow("Price", price = new DoubleTextField());
        }


        void reset(){
            price.setNumber(0d);
            name.setText(null);
            invoiceNumber.setText(null);
        }

        public void setPrice(Double price){
            this.price.setNumber(price);
        }

        public void setName(String name){
            this.name.setText(name);
        }

        public void setInvoiceNumber(String invoiceNumber){
            this.invoiceNumber.setText(invoiceNumber);
        }

        public Double getPrice(){
            return this.price.getNumber();
        }

        public String getName(){
            return this.name.getText();
        }

        public String getInvoiceNumber(){
            return this.invoiceNumber.getText();
        }
    }

}
