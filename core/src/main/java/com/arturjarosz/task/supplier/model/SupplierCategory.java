package com.arturjarosz.task.supplier.model;

import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntry;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;

/**
 * Category of a Supplier (e.g. a paint shop), configured by users. A category used by any Supplier cannot be removed.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "supplier_category_sequence", allocationSize = 1)
@Table(name = "SUPPLIER_CATEGORY")
public class SupplierCategory extends AbstractNamedEntry {
    @Serial
    private static final long serialVersionUID = 5830149274861203755L;

    protected SupplierCategory() {
        // needed by JPA
    }

    public SupplierCategory(String name) {
        super(name);
    }
}
