package com.arturjarosz.task.sharedkernel.status;

/**
 * Interface for status change validators.
 */
public interface StatusTransitionValidator<TStatusTransition extends StatusTransition> {

    TStatusTransition getStatusTransition();
}
