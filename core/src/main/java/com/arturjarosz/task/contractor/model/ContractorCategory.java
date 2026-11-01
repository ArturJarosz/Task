package com.arturjarosz.task.contractor.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serial;

/**
 * Category of a Contractor (e.g. a plumber), configured by users. A category used by any Contractor cannot be removed.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "contractor_category_sequence", allocationSize = 1)
@Table(name = "CONTRACTOR_CATEGORY")
public class ContractorCategory extends AbstractAggregateRoot {
    @Serial
    private static final long serialVersionUID = -4127305658326318012L;

    public static final int NAME_MAX_LENGTH = 100;

    @Getter
    @Column(name = "NAME", nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    protected ContractorCategory() {
        // needed by JPA
    }

    public ContractorCategory(String name) {
        this.name = name;
    }

    public void rename(String name) {
        this.name = name;
    }
}
