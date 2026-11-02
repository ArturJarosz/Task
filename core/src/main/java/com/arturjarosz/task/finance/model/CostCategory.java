package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntry;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;

/**
 * Category of a Cost (e.g. fuel), configured by users. A category used by any Cost cannot be removed.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "cost_category_sequence", allocationSize = 1)
@Table(name = "COST_CATEGORY")
public class CostCategory extends AbstractNamedEntry {
    @Serial
    private static final long serialVersionUID = 6203819475523017741L;

    protected CostCategory() {
        // needed by JPA
    }

    public CostCategory(String name) {
        super(name);
    }
}
