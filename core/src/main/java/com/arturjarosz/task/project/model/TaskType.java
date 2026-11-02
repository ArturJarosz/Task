package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntry;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;

/**
 * Type of the Task (e.g. render), configured by users. A type used by any Task cannot be removed.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "task_type_sequence", allocationSize = 1)
@Table(name = "TASK_TYPE")
public class TaskType extends AbstractNamedEntry {
    @Serial
    private static final long serialVersionUID = -6618230485531907442L;

    protected TaskType() {
        // needed by JPA
    }

    public TaskType(String name) {
        super(name);
    }
}
