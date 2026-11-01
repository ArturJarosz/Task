package com.arturjarosz.task.supplier.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serial;

/**
 * Category of a Supplier (e.g. a paint shop), configured by users. A category used by any Supplier cannot be removed.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "supplier_category_sequence", allocationSize = 1)
@Table(name = "SUPPLIER_CATEGORY")
public class SupplierCategory extends AbstractAggregateRoot {
    @Serial
    private static final long serialVersionUID = 5830149274861203755L;

    public static final int NAME_MAX_LENGTH = 100;

    @Getter
    @Column(name = "NAME", nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    protected SupplierCategory() {
        // needed by JPA
    }

    public SupplierCategory(String name) {
        this.name = name;
    }

    public void rename(String name) {
        this.name = name;
    }
}
