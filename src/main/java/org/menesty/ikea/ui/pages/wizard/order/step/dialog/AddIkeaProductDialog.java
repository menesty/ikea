package org.menesty.ikea.ui.pages.wizard.order.step.dialog;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.BaseEntityDialog;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.form.NumberTextField;
import org.menesty.ikea.ui.controls.form.ProductIdField;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 9/11/15.
 * 10:02.
 */
//TODO add loading panel during load product from ikea site better run it in background task
public class AddIkeaProductDialog extends BaseEntityDialog<IkeaOrderItem> {
    private IkeaProductAddForm ikeaProductAddForm;

    public AddIkeaProductDialog(Stage stage) {
        super(stage);
        setTitle(I18n.UA.getString(I18nKeys.ADD_IKEA_PRODUCT));

        ikeaProductAddForm = new IkeaProductAddForm();

        okBtn.setDisable(true);
        okBtn.setText(I18n.UA.getString(I18nKeys.ADD));
        ikeaProductAddForm.validProperty().addListener((observable, oldValue, newValue) -> okBtn.setDisable(!newValue));

        addRow(ikeaProductAddForm, bottomBar);
    }

    @Override
    protected IkeaOrderItem collect() {
        return new IkeaOrderItem(ikeaProductAddForm.getIkeaProduct(), ikeaProductAddForm.getCount(),
                ikeaProductAddForm.getIkeaProduct().getPrice());
    }

    @Override
    protected void populate(IkeaOrderItem entityValue) {

    }

    @Override
    public boolean isValid() {
        return ikeaProductAddForm.isValid();
    }

    @Override
    public void reset() {
        ikeaProductAddForm.reset();
    }


    class IkeaProductAddForm extends FormPane {
        private ProductIdField productIdField;
        private NumberTextField countField;
        private IkeaProduct ikeaProduct;
        private BooleanProperty validProperty;


        public IkeaProductAddForm() {
            validProperty = new SimpleBooleanProperty();

            add(productIdField = new ProductIdField(I18n.UA.getString(I18nKeys.ART_NUMBER), false));
            add(countField = new NumberTextField(I18n.UA.getString(I18nKeys.COUNT), false));

            productIdField.validProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    try {
                        ikeaProduct = ServiceFacade.getIkeaProductService().getProduct(productIdField.getProductId(), true, throwable -> ServiceFacade.getErrorConsole().add(throwable));
                    } catch (Exception e) {
                        ikeaProduct = null;
                    }
                } else {
                    ikeaProduct = null;
                }

                validProperty.set(isValid());
            });
            countField.textProperty().addListener(event -> validProperty.set(isValid()));
        }

        @Override
        public boolean isValid() {
            return super.isValid() && ikeaProduct != null;
        }

        public IkeaProduct getIkeaProduct() {
            return ikeaProduct;
        }

        public BigDecimal getCount() {
            return countField.getNumber();
        }

        public BooleanProperty validProperty() {
            return validProperty;
        }
    }
}
