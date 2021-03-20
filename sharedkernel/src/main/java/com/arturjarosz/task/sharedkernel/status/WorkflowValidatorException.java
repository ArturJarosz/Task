package com.arturjarosz.task.sharedkernel.status;

import com.arturjarosz.task.sharedkernel.exceptions.BaseRuntimeException;

public class WorkflowValidatorException extends BaseRuntimeException {
    private static final long serialVersionUID = 4945909768428038726L;

    public WorkflowValidatorException(String message, Object... messageParameters) {
        super(message, messageParameters);
    }
}
