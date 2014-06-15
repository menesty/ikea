package org.menesty.ikea.ui.controls;

import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.pages.EntityDialogCallback;

/**
 * Created by Menesty on
 * 6/15/14.
 * 16:34.
 */
public abstract class BaseEntityDialog<Entity> extends BaseDialog implements Form {

    private EntityDialogCallback<Entity> callback;

    protected Entity entityValue;

    public void setCallback(EntityDialogCallback<Entity> callback) {
        this.callback = callback;
    }

    @Override
    public void onCancel() {
        if (callback != null)
            callback.onCancel();
    }

    @Override
    public void onOk() {
        if (isValid())
            onSave(collect());
    }

    protected abstract Entity collect();

    protected abstract void populate(Entity entityValue);

    private void onSave(Entity currentEntity) {
        if (callback != null)
            callback.onSave(currentEntity);
    }

    public void bind(Entity entityValue, EntityDialogCallback<Entity> callback) {
        this.entityValue = entityValue;
        this.callback = callback;
        reset();

        populate(entityValue);
    }
}
