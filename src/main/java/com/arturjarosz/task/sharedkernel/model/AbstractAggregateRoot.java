package com.arturjarosz.task.sharedkernel.model;

import javax.persistence.MappedSuperclass;

/**
 * Base class for all aggregate roots.
 */

@MappedSuperclass
public abstract class AbstractAggregateRoot extends AbstractEntity {
    private static final long serialVersionUID = -1097600977230597412L;

}
