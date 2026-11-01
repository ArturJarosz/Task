package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serial;

/**
 * Category of a Cost (e.g. fuel), configured by users. A category used by any Cost cannot be removed.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "cost_category_sequence", allocationSize = 1)
@Table(name = "COST_CATEGORY")
public class CostCategory extends AbstractAggregateRoot {
    @Serial
    private static final long serialVersionUID = 6203819475523017741L;

    public static final int NAME_MAX_LENGTH = 100;

    @Getter
    @Column(name = "NAME", nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    protected CostCategory() {
        // needed by JPA
    }

    public CostCategory(String name) {
        this.name = name;
    }

    public void rename(String name) {
        this.name = name;
    }
}
