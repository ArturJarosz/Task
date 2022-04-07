package com.arturjarosz.task.sharedkernel.status;

import java.util.Collection;

/**
 * Interface for all statuses on workflows.
 *
 * @param <TStatus>
 */
public interface Status<TStatus extends Status> {
    /**
     * Collection of possible Statuses, that current status can transition to.
     */
    Collection<TStatus> getPossibleStatusTransitions();

    String getStatusName();
}
