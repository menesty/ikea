package org.menesty.ikea.ui.pages.ikea.order.dialog.returnitem;

import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StockItemDto;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.form.LabelField;
import org.menesty.ikea.ui.controls.form.NumberTextField;
import org.menesty.ikea.ui.pages.EntityDialogCallback;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 6/16/16.
 * 12:54.
 */
public class ReturnItemDialog extends BaseDialog {
  private ReturnItemForm returnItemForm;
  private EntityDialogCallback<BigDecimal> callback;

  public ReturnItemDialog(Stage stage) {
    super(stage);
    setTitle(I18n.UA.getString(I18nKeys.RETURN_BACK_INVOICE_ITEM_ORDER));
    addRow(returnItemForm = new ReturnItemForm());
    addRow(bottomBar);
  }

  public void setEntity(StockItemDto entity, EntityDialogCallback<BigDecimal> callback) {
    this.callback = callback;

    returnItemForm.setArtNumber(entity.getArtNumber());
    returnItemForm.setMaxCount(entity.getCount());
  }

  @Override
  public void onOk() {
    if (returnItemForm.isValid()) {
      callback.onSave(returnItemForm.getCount());
    }
  }

  @Override
  public void onCancel() {
    callback.onCancel();
  }

  class ReturnItemForm extends FormPane {
    private LabelField artNumberField;
    private NumberTextField itemCountField;

    public ReturnItemForm() {
      add(artNumberField = new LabelField(I18n.UA.getString(I18nKeys.ART_NUMBER)));
      add(itemCountField = new NumberTextField(I18n.UA.getString(I18nKeys.COUNT), false));
      itemCountField.addValidationRule(value -> value != null && BigDecimal.ZERO.compareTo((BigDecimal) value) == -1);
    }

    public void setArtNumber(String artNumber) {
      artNumberField.setText(artNumber);
    }

    public BigDecimal getCount() {
      return itemCountField.getNumber();
    }

    public void setMaxCount(BigDecimal maxCount) {
      itemCountField.setMaxValue(maxCount);

      if (maxCount.doubleValue() % 1 == 0) {
        itemCountField.setAllowDouble(false);
        itemCountField.setMinValue(BigDecimal.ONE);
      } else {
        itemCountField.setAllowDouble(true);
        itemCountField.setMinValue(null);
      }
    }
  }
}
