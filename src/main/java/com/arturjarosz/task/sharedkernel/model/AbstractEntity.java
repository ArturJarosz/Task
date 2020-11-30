package com.arturjarosz.task.sharedkernel.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.UUID;

/**
 * Base class for Entities providing id.
 */

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {
    private static final long serialVersionUID = -1221112071690776121L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "UUID", nullable = false)
    protected UUID uuid = UUID.randomUUID();

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
