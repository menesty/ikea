package org.menesty.ikea.dto;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Menesty on
 * 9/6/15.
 * 20:17.
 */
public class ErrorItem {
    private final StringProperty message = new SimpleStringProperty();
    private final StringProperty details = new SimpleStringProperty();

    public ErrorItem(Throwable throwable) {
        this.message.set(throwable.getMessage());

        StringWriter errors = new StringWriter();
        throwable.printStackTrace(new PrintWriter(errors));

        this.details.set(errors.toString());
    }

    public String getMessage() {
        return message.get();
    }

    public String getDetails() {
        return details.get();
    }

}
