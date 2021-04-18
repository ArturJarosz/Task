package com.arturjarosz.task.sharedkernel.status;

import javax.annotation.Nullable;

/**
 * Interface for status change validators.
 */
public interface StatusTransitionValidator<TStatusTransition extends StatusTransition> {

    TStatusTransition getStatusTransition();
}
