package org.menesty.ikea.ui.pages.ikea.order.dialog.invoice;

import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.ui.controls.dialog.EntityDialog;
import org.menesty.ikea.ui.controls.form.NumberTextField;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 2/21/16.
 * 17:33.
 */
public class InvoiceItemChangeCountDialog extends EntityDialog<BigDecimal> {
  public InvoiceItemChangeCountDialog(Stage stage) {
    super(stage);
    setTitle(I18n.UA.getString(I18nKeys.CHANGE_ITEM_COUNT));
    addRow(getEntityForm(), bottomBar);
  }

  @Override
  protected EntityForm<BigDecimal> createForm() {
    return new InvoiceItemCountForm();
  }


  class InvoiceItemCountForm extends EntityForm<BigDecimal> {
    private NumberTextField countField;

    public InvoiceItemCountForm() {
      add(countField = new NumberTextField(null, I18n.UA.getString(I18nKeys.COUNT), false));
    }

    @Override
    protected BigDecimal collect(BigDecimal entity) {
      return countField.getNumber();
    }

    @Override
    protected void populate(BigDecimal entity) {
      countField.setNumber(entity);
    }
  }
}
