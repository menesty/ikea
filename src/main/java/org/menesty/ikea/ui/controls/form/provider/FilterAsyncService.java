package org.menesty.ikea.ui.controls.form.provider;

import javafx.concurrent.Task;
import org.menesty.ikea.service.AbstractAsyncService;

/**
 * Created by Menesty on
 * 9/6/15.
 * 00:39.
 */
public abstract class FilterAsyncService<T> extends AbstractAsyncService<T> {
    private String filterQuery;

    public void setFilterQuery(String filterQuery) {
        this.filterQuery = filterQuery;
    }

    @Override
    protected Task<T> createTask() {
        return createTask(filterQuery);
    }

    public abstract Task<T> createTask(String string);
}
