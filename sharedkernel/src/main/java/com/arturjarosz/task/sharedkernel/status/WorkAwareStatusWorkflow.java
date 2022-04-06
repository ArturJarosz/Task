package com.arturjarosz.task.sharedkernel.status;

import java.util.Set;

/**
 * Interface that has to be implemented by StatusWorkflow service. This interface is
 * responsible for providing statuses, that can be used for progressing with work or creating new Project objects,
 * such as Tasks or Stages.
 */
public interface WorkAwareStatusWorkflow<TStatus extends Status> {
    /**
     * Returns all statuses, that allows for any work on Project, such as changing Project statuses.
     */
    Set<TStatus> getStatusesThatAllowWorking();

    /**
     * Returns all statuses, that allows for creating new Project objects, such as Tasks or Stages.
     */
    Set<TStatus> getStatusesThatAllowCreatingWorkObjects();
}
