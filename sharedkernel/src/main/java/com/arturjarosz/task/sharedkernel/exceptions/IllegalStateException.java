package com.arturjarosz.task.sharedkernel.exceptions;

import java.io.Serial;

public class IllegalStateException extends BaseRuntimeException {
    @Serial
    private static final long serialVersionUID = -6149413430368693629L;

    public IllegalStateException() {
    }

    public IllegalStateException(Exception exception) {
        super(exception);
    }

    public IllegalStateException(String message) {
        super(message);
    }

    public IllegalStateException(String message, Exception exception) {
        super(message, exception);
    }

    public IllegalStateException(String message, Exception exception, Object... messageParameters) {
        super(message, exception, messageParameters);
    }

    public IllegalStateException(String message, Object... messageParameters) {
        super(message, messageParameters);
    }
}
