package org.menesty.ikea.ui.pages.ikea.order.dialog.invoice;

import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.invoice.Invoice;
import org.menesty.ikea.ui.controls.dialog.EntityDialog;
import org.menesty.ikea.ui.controls.form.NumberTextField;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.form.WrapField;
import org.menesty.ikea.util.DateUtil;

import java.time.LocalDate;

/**
 * Created by Menesty on
 * 10/8/15.
 * 13:02.
 */
public class InvoiceAddEditDialog extends EntityDialog<Invoice> {
  public InvoiceAddEditDialog(Stage stage) {
    super(stage);
    setAllowAutoHide(false);

    addRow(getEntityForm(), bottomBar);
  }

  @Override
  protected EntityDialog<Invoice>.EntityForm<Invoice> createForm() {
    return new InvoiceForm();
  }

  @Override
  protected void populate(Invoice entityValue) {
    super.populate(entityValue);

    if (entityValue.getId() == null) {
      setTitle(I18n.UA.getString(I18nKeys.INVOICE_ADD_TITLE));
    } else {
      setTitle(I18n.UA.getString(I18nKeys.INVOICE_EDIT_TITLE));
    }
  }

  class InvoiceForm extends EntityForm<Invoice> {
    private DatePicker sellDate;
    private TextField invoiceNameField;
    private TextField paragonNumberField;
    private NumberTextField payedField;

    public InvoiceForm() {
      add(invoiceNameField = new TextField(null, I18n.UA.getString(I18nKeys.INVOICE_NAME), false));
      add(paragonNumberField = new TextField(null, I18n.UA.getString(I18nKeys.PARAGON_NUMBER)));
      add(payedField = new NumberTextField(I18n.UA.getString(I18nKeys.AMOUNT), false));
      add(new WrapField<DatePicker>(I18n.UA.getString(I18nKeys.SELL_DATE), sellDate = new DatePicker()) {
        @Override
        public boolean isValid() {
          return sellDate.getValue() != null;
        }

        @Override
        public void reset() {
          sellDate.setValue(null);
        }
      });
    }

    @Override
    protected Invoice collect(Invoice entity) {
      entity.setParagonNumber(paragonNumberField.getText());
      entity.setInvoiceName(invoiceNameField.getText());
      entity.setPayed(payedField.getNumber());
      entity.setSellDate(DateUtil.toDate(sellDate.getValue()));

      return entity;
    }

    @Override
    protected void populate(Invoice entity) {
      sellDate.setValue(entity.getSellDate() != null ? LocalDate.parse(entity.getSellDate().toString()) : null);
      invoiceNameField.setText(entity.getInvoiceName());
      paragonNumberField.setText(entity.getParagonNumber());
      payedField.setNumber(entity.getPayed());
    }
  }
}
