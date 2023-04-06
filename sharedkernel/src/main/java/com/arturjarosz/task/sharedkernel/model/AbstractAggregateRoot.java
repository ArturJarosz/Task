package com.arturjarosz.task.sharedkernel.model;

import javax.persistence.MappedSuperclass;
import java.io.Serial;

/**
 * Base class for all aggregate roots.
 */

@MappedSuperclass
public abstract class AbstractAggregateRoot extends AbstractEntity {
    @Serial
    private static final long serialVersionUID = -1097600977230597412L;

}
