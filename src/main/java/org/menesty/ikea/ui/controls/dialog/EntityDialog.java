package org.menesty.ikea.ui.controls.dialog;

import javafx.stage.Stage;
import org.menesty.ikea.ui.controls.BaseEntityDialog;
import org.menesty.ikea.ui.controls.form.FormPane;

/**
 * Created by Menesty on
 * 10/8/15.
 * 13:10.
 */
public abstract class EntityDialog<Entity> extends BaseEntityDialog<Entity> {
    private EntityForm<Entity> entityForm;

    public EntityDialog(Stage stage) {
        super(stage);
        entityForm = createForm();
    }

    protected EntityForm<Entity> getEntityForm() {
        return entityForm;
    }

    @Override
    protected Entity collect() {
        return entityForm.collect(entityValue);
    }

    @Override
    protected void populate(Entity entityValue) {
        entityForm.populate(entityValue);
    }

    @Override
    public boolean isValid() {
        return entityForm.isValid();
    }

    @Override
    public void reset() {
        entityForm.reset();
    }

    protected abstract EntityForm<Entity> createForm();

    public abstract class EntityForm<T> extends FormPane {
        protected abstract T collect(T entity);

        protected abstract void populate(T entity);
    }
}
