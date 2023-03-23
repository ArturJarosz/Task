package com.arturjarosz.task.sharedkernel.model;

import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;

/**
 * Base class for all people classes.
 */

@MappedSuperclass
public abstract class AbstractPerson extends AbstractEntity {
    private static final long serialVersionUID = -760258260740065198L;

    @Embedded
    private PersonName name;

    protected AbstractPerson() {
        // needed by JPA
    }

    protected AbstractPerson(PersonName name) {
        this.name = name;
    }

    public PersonName getName() {
        return this.name;
    }

    public void setName(PersonName name) {
        this.name = name;
    }
}
