package com.arturjarosz.task.sharedkernel.status;

/**
 * Service that is responsible for changing Status for WorkflowAware object.
 *
 * @param <TStatus>
 * @param <TStatusTransition>
 * @param <TObject>
 */
public interface WorkflowService<TStatus extends Status, TStatusTransition extends StatusTransition, TObject extends WorkflowAware> {
    void changeStatus(TObject object, TStatus status);

    void beforeStatusChange(TObject object, TStatusTransition statusTransition);

}
