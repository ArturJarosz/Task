package com.arturjarosz.task.sharedkernel.status;

/**
 * Interface for status change validators.
 */
public interface StatusTransitionValidator<T extends StatusTransition> {

    T getStatusTransition();
}
