package org.menesty.ikea.service.parser;

/**
 * Created by Menesty on
 * 9/7/15.
 * 21:18.
 */
public class ErrorMessage {
    private String message;

    private String exception;

    public ErrorMessage(String message, String exception) {
        this.message = message;
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}
