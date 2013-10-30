package org.menesty.ikea.ui.pages;

public abstract class DialogCallback<Entity> {
    public abstract void onSave(Entity entity, Object... params);

    public abstract void onCancel();
}
