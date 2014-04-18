package org.menesty.ikea.ui.controls.dialog;

import javafx.scene.control.TextField;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.ui.controls.form.DoubleTextField;
import org.menesty.ikea.ui.layout.RowPanel;
import org.menesty.ikea.ui.pages.EntityDialogCallback;

import java.util.UUID;

public class InvoiceItemDialog extends BaseDialog {

    private InvoiceItem currentItem;

    private EntityDialogCallback<InvoiceItem> callback;

    private InvoiceItemForm form;

    public InvoiceItemDialog() {
        setTitle("Create invoice");

        addRow(form = new InvoiceItemForm(), bottomBar);
        okBtn.setText("Save");
    }

    @Override
    public void onOk() {
        currentItem.setPrice(form.getPrice());
        currentItem.basePrice = form.getPrice();
        currentItem.setName(form.getName());
        currentItem.setShortName(form.getShortName());

        if (currentItem.getId() == null)
            currentItem.setOriginArtNumber(UUID.randomUUID().toString());

        currentItem.setArtNumber(form.getOriginalArtNumber());

        if (callback != null)
            callback.onSave(currentItem);
    }

    @Override
    public void onCancel() {
        if (callback != null)
            callback.onCancel();
    }

    public void bind(InvoiceItem invoiceItem, EntityDialogCallback<InvoiceItem> callback) {
        currentItem = invoiceItem;
        this.callback = callback;

        form.reset();
        form.setPrice(invoiceItem.getPriceWat());
        form.setName(invoiceItem.getName());
        form.setShortName(invoiceItem.getShortName());
        form.setOriginalArtNumber(invoiceItem.getArtNumber());

    }


    class InvoiceItemForm extends RowPanel {
        private TextField originalArtNumber;

        private DoubleTextField basePrice;

        private TextField shortName;

        private TextField name;


        public InvoiceItemForm() {
            addRow("Art Number", originalArtNumber = new TextField());
            addRow("Name", name = new TextField());
            addRow("Short name", shortName = new TextField());
            addRow("Price", basePrice = new DoubleTextField());
        }

        void setShortName(String shortName) {
            this.shortName.setText(shortName);
        }

        String getShortName() {
            return shortName.getText();
        }

        void setOriginalArtNumber(String originArtNumber) {
            this.originalArtNumber.setText(originArtNumber);
        }

        String getOriginalArtNumber() {
            return originalArtNumber.getText();
        }

        void setPrice(double price) {
            basePrice.setNumber(price);
        }

        double getPrice() {
            return basePrice.getNumber();
        }

        void setName(String name) {
            this.name.setText(name);
        }

        String getName() {
            return name.getText();
        }

        void reset() {
            originalArtNumber.clear();
            basePrice.clear();
            shortName.clear();
            name.clear();
        }
    }
}