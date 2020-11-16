package com.arturjarosz.task.sharedkernel.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Base class for Entities providing id.
 */

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {
    private static final long serialVersionUID = -1221112071690776121L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "Abs_ent", initialValue = 100)
    private Long id;

    public Long getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        AbstractEntity other = (AbstractEntity) obj;

        return this.id.equals(other.id);
    }

}
