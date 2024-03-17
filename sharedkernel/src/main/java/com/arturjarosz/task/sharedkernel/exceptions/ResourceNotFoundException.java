package com.arturjarosz.task.sharedkernel.exceptions;

import java.io.Serial;

public class ResourceNotFoundException extends BaseRuntimeException {
    @Serial
    private static final long serialVersionUID = 4337123767626580964L;

    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String messageCode, Object[] parameters) {
        super(messageCode, parameters);
    }
}
