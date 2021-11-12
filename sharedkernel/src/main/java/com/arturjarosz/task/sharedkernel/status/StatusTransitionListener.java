package com.arturjarosz.task.sharedkernel.status;

/**
 * Interface for status change listeners. They should be triggers, after status change has occurred. For example,
 * when Task status was changed and that change should affect Stage status suitable listener should be triggered.
 */
public interface StatusTransitionListener<TStatusTransition extends StatusTransition> {

    TStatusTransition getStatusTransition();
}
