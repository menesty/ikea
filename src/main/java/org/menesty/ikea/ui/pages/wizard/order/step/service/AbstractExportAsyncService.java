package org.menesty.ikea.ui.pages.wizard.order.step.service;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.service.AbstractAsyncService;

import java.io.File;

/**
 * Created by Menesty on
 * 9/12/15.
 * 00:48.
 */
public abstract class AbstractExportAsyncService<T> extends AbstractAsyncService<Void> {
    private ObjectProperty<File> fileFileProperty = new SimpleObjectProperty<>();
    private ObjectProperty<T> param = new SimpleObjectProperty<>();

    public void setParam(T param) {
        this.param.set(param);
    }

    public void setFile(File file) {
        fileFileProperty.set(file);
    }

    @Override
    protected Task<Void> createTask() {
        final File _file = fileFileProperty.get();
        final T _param = param.get();

        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                export(_file, _param);
                return null;
            }
        };
    }


    protected abstract void export(File file, T param);
}
