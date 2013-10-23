package org.menesty.ikea.ui.pages;

import org.menesty.ikea.domain.ProductInfo;

public abstract class DialogCallback {
    public abstract void onSave(ProductInfo productInfo, boolean isCombo);

    public abstract void onCancel();
}
