package com.arturjarosz.task.sharedkernel.exceptions;

public class SampleDataException extends BaseRuntimeException {
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
