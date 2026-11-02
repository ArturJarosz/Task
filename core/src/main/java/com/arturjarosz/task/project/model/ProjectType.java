package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntry;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;

/**
 * Type of the Project (e.g. interior of an apartment), configured by users. A type used by any Project cannot be
 * removed.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_type_sequence", allocationSize = 1)
@Table(name = "PROJECT_TYPE")
public class ProjectType extends AbstractNamedEntry {
    @Serial
    private static final long serialVersionUID = -2304538212907161245L;

    protected ProjectType() {
        // needed by JPA
    }

    public ProjectType(String name) {
        super(name);
    }
}
