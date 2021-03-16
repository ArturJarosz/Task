package com.arturjarosz.task.sharedkernel.status;

/**
 * Interface for status change validators.
 */
public interface StatusTransitionValidator<TStatusTransition extends StatusTransition, TWorkflowAware extends WorkflowAware> {
    void validate(TWorkflowAware object, TStatusTransition statusTransition);

    TStatusTransition getStatusTransition();
}
