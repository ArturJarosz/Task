package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serial;

/**
 * Type of the Project (e.g. interior of an apartment), configured by users. A type used by any Project cannot be
 * removed.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_type_sequence", allocationSize = 1)
@Table(name = "PROJECT_TYPE")
public class ProjectType extends AbstractAggregateRoot {
    @Serial
    private static final long serialVersionUID = -2304538212907161245L;

    public static final int NAME_MAX_LENGTH = 100;

    @Getter
    @Column(name = "NAME", nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    protected ProjectType() {
        // needed by JPA
    }

    public ProjectType(String name) {
        this.name = name;
    }

    public void rename(String name) {
        this.name = name;
    }
}
