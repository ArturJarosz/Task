package com.arturjarosz.task.sharedkernel.model;

import jakarta.persistence.MappedSuperclass;

import java.io.Serial;

/**
 * Base class for all aggregate roots.
 */

@MappedSuperclass
public abstract class AbstractAggregateRoot extends AbstractHistoryAwareEntity {
    @Serial
    private static final long serialVersionUID = -1097600977230597412L;

}
