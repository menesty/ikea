package org.menesty.ikea.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Created by Menesty on
 * 9/6/15.
 * 15:06.
 */
public class ErrorConsole {
    private ObjectProperty<ObservableList<Throwable>> errors = new SimpleObjectProperty<>(this, "errors");

    public ErrorConsole() {
        errors.set(FXCollections.observableArrayList());
    }

    public void add(Throwable throwable) {
        errors.get().add(0, throwable);
    }

    public ObjectProperty<ObservableList<Throwable>> errorsProperty() {
        return errors;
    }

    public List<Throwable> getItems() {
        return errors.get();
    }

    public void clear(){
        errors.get().removeAll(errors.get());
    }
}
