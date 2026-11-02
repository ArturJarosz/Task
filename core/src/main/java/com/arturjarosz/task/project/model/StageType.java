package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntry;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;

/**
 * Type of the Stage (e.g. visualisations), configured by users. A type used by any Stage cannot be removed.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "stage_type_sequence", allocationSize = 1)
@Table(name = "STAGE_TYPE")
public class StageType extends AbstractNamedEntry {
    @Serial
    private static final long serialVersionUID = 4127730918264155093L;

    protected StageType() {
        // needed by JPA
    }

    public StageType(String name) {
        super(name);
    }
}
