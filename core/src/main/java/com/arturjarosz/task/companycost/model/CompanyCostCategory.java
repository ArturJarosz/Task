package com.arturjarosz.task.companycost.model;

import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntry;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;

/**
 * Category of a CompanyCost (e.g. software), configured by users. A category used by any CompanyCost cannot be
 * removed.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "company_cost_category_sequence", allocationSize = 1)
@Table(name = "COMPANY_COST_CATEGORY")
public class CompanyCostCategory extends AbstractNamedEntry {
    @Serial
    private static final long serialVersionUID = 4419827305128834461L;

    protected CompanyCostCategory() {
        // needed by JPA
    }

    public CompanyCostCategory(String name) {
        super(name);
    }
}
