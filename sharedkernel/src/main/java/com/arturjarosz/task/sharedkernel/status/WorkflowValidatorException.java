package com.arturjarosz.task.sharedkernel.status;

import com.arturjarosz.task.sharedkernel.exceptions.BaseRuntimeException;

import java.io.Serial;

public class WorkflowValidatorException extends BaseRuntimeException {
    @Serial
    private static final long serialVersionUID = 4945909768428038726L;

    public WorkflowValidatorException(String message, Object... messageParameters) {
        super(message, messageParameters);
    }
}
