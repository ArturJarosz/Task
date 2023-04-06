package com.arturjarosz.task.sharedkernel.exceptions;

import java.io.Serial;

public class IllegalOperationException extends BaseRuntimeException {
    @Serial
    private static final long serialVersionUID = 6299214753231118497L;

    public IllegalOperationException() {
    }

    public IllegalOperationException(Exception exception) {
        super(exception);
    }

    public IllegalOperationException(String message) {
        super(message);
    }

    public IllegalOperationException(String message, Exception exception) {
        super(message, exception);
    }

    public IllegalOperationException(String message, Exception exception, Object... messageParameters) {
        super(message, exception, messageParameters);
    }

    public IllegalOperationException(String message, Object... messageParameters) {
        super(message, messageParameters);
    }
}
