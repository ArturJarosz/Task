package com.arturjarosz.task.sharedkernel.status;

/**
 * Service that is responsible for changing Status for WorkflowAware object.
 *
 * @param <TStatus>
 * @param <TObject>
 */
public interface WorkflowService<TStatus extends Status, TObject extends WorkflowAware> {
    void changeStatus(TObject object, TStatus status);
}
