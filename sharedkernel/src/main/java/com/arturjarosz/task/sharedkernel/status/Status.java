package com.arturjarosz.task.sharedkernel.status;

import java.util.Collection;

/**
 * Interface for all statuses on workflows.
 *
 * @param <T>
 */
public interface Status<T extends Status> {
    /**
     * Collection of possible Statuses, that current status can transition to.
     */
    Collection<T> getPossibleStatusTransitions();

    String getStatusName();
}
