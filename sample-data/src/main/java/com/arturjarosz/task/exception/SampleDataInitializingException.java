package com.arturjarosz.task.exception;

import java.io.Serial;

public class SampleDataInitializingException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4336501143564462331L;

    public SampleDataInitializingException(String message, Throwable cause) {
        super(message, cause);
    }
}
