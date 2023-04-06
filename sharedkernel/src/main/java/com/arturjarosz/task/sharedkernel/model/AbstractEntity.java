package com.arturjarosz.task.sharedkernel.model;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Base class for Entities providing id.
 */

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -1221112071690776121L;

    private static final String SEQUENCE_NAME = "sequence_generator";
    @Column(name = "UUID", nullable = false)
    protected final UUID uuid = UUID.randomUUID();
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
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

        return this.uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }
}
