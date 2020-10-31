package com.arturjarosz.application.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Base class for Entities providing id.
 */

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {
    private static final long serialVersionUID = -1221112071690776121L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    public Long getId() {
        return this.id;
    }
}
