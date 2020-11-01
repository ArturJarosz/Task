package com.arturjarosz.application.exceptions;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Basic class for all custom runtime exceptions
 */
public class BaseRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -2333120646773512436L;

    private Object[] messageParameters;

    protected BaseRuntimeException() {
    }

    public BaseRuntimeException(Exception exception) {
        super(exception);
    }

    public BaseRuntimeException(String message) {
        super(message);
    }

    public BaseRuntimeException(String message, Exception exception) {
        super(message, exception);
    }

    public BaseRuntimeException(String message, Exception exception, Object... messageParameters) {
        super(message, exception);
        this.messageParameters = messageParameters;
    }

    public BaseRuntimeException(String message, Object... messageParameters) {
        super(message);
        this.messageParameters = ArrayUtils.nullToEmpty(messageParameters);
    }

    public Object[] getMessageParameters() {
        return this.messageParameters;
    }
}
