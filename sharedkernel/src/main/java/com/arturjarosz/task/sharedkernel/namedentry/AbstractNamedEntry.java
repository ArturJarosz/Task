package com.arturjarosz.task.sharedkernel.namedentry;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.io.Serial;

/**
 * Entry of a list configured by users that is only a name (e.g. a project type or a cost category). Names are unique
 * regardless of letter case, and an entry used by any item cannot be removed.
 */
@MappedSuperclass
public abstract class AbstractNamedEntry extends AbstractAggregateRoot {
    @Serial
    private static final long serialVersionUID = 2873046113562708245L;

    public static final int NAME_MAX_LENGTH = 100;

    @Getter
    @Column(name = "NAME", nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    protected AbstractNamedEntry() {
        // needed by JPA
    }

    protected AbstractNamedEntry(String name) {
        this.name = name;
    }

    public void rename(String name) {
        this.name = name;
    }
}
