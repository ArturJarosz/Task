package com.arturjarosz.task.sharedkernel.status;

/**
 * Interface for Objects, that can store, and change their statuses according to appropriate Workflow.
 */
public interface WorkflowAware<T extends Status> {
    /**
     * Return current status of the WorkflowAware object.
     */
    T getStatus();

    /**
     * Return name of current Workflow of the Object.
     */
    String getWorkflowName();

    /**
     * Changes current status to new one provided in parameter.
     */
    void changeStatus(T status);
}
