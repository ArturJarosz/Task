package com.arturjarosz.task.sharedkernel.exceptions;

import java.io.Serial;

public class SampleDataException extends BaseRuntimeException {
    @Serial
    private static final long serialVersionUID = -4243990529954076545L;

    public SampleDataException() {
    }

    public SampleDataException(Exception exception) {
        super(exception);
    }

    public SampleDataException(String message) {
        super(message);
    }

    public SampleDataException(String message, Exception exception) {
        super(message, exception);
    }
}
