package com.arturjarosz.application.exceptions;

public class IllegalArgumentException extends BaseRuntimeException {

    private static final long serialVersionUID = -9037086014795116707L;

    public IllegalArgumentException() {
    }

    public IllegalArgumentException(Exception exception) {
        super(exception);
    }

    public IllegalArgumentException(String message) {
        super(message);
    }

    public IllegalArgumentException(String message, Exception exception) {
        super(message, exception);
    }

    public IllegalArgumentException(String message, Exception exception, Object... messageParameters) {
        super(message, exception, messageParameters);
    }

    public IllegalArgumentException(String message, Object... messageParameters) {
        super(message, messageParameters);
    }
}
