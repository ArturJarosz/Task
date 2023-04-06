package com.arturjarosz.task.sharedkernel.exceptions;

import java.io.Serial;

/**
 * Basic exception for wrong arguments.
 */
public class IllegalArgumentException extends BaseRuntimeException {
    @Serial
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
