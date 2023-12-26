package com.arturjarosz.task.sharedkernel.status;

/**
 * Service that is responsible for changing Status for WorkflowAware object.
 *
 * @param <T>
 * @param <U>
 */
public interface WorkflowService<T extends Status, U extends WorkflowAware> {
    void changeStatus(U object, T status);
}
