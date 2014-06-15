package org.menesty.ikea.ui.controls.dialog;

import org.menesty.ikea.domain.IkeaShop;
import org.menesty.ikea.ui.controls.BaseEntityDialog;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.form.NumberTextField;
import org.menesty.ikea.ui.controls.form.TextField;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 6/15/14.
 * 16:30.
 */
public class IkeaShopDialog extends BaseEntityDialog<IkeaShop> {
    private IkeaShopForm form;

    public IkeaShopDialog() {
        addRow(form = new IkeaShopForm(), bottomBar);
        okBtn.setText("Save");
    }

    @Override
    protected IkeaShop collect() {
        entityValue.setName(form.getName());
        entityValue.setShopId(form.getShopId().intValue());
        return entityValue;
    }

    @Override
    protected void populate(IkeaShop entityValue) {
        form.setName(entityValue.getName());
        form.setShopId(entityValue.getShopId());
    }

    @Override
    public boolean isValid() {
        return form.isValid();
    }

    @Override
    public void reset() {
        form.reset();
    }

    class IkeaShopForm extends FormPane {
        private TextField name;
        private NumberTextField shopId;

        public IkeaShopForm() {
            add(name = new TextField(null, "Name", false));
            add(shopId = new NumberTextField("Shop ID", false));
            shopId.setAllowDouble(false);
        }

        String getName() {
            return name.getText();
        }

        BigDecimal getShopId() {
            return shopId.getNumber();
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setShopId(int shopId) {
            this.shopId.setNumber(BigDecimal.valueOf(shopId));
        }
    }
}
