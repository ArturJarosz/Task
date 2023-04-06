package com.arturjarosz.task.sharedkernel.model;

import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import java.io.Serial;

/**
 * Base class for all people classes.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@MappedSuperclass
public abstract class AbstractPerson extends AbstractEntity {
    @Serial
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
