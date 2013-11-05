package org.menesty.ikea.exception;

public class LoginIkeaException extends RuntimeException {

    public LoginIkeaException(String message) {
        super(message);
    }

    public LoginIkeaException(Throwable cause) {
        super(cause);
    }
}
