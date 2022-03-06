package com.arturjarosz.task.supplier.query.impl;

import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.supplier.model.QSupplier;
import com.arturjarosz.task.supplier.query.SupplierQueryService;

@Finder
public class SupplierQueryServiceImpl extends AbstractQueryService<QSupplier> implements SupplierQueryService {

    private static final QSupplier SUPPLIER = QSupplier.supplier;

    public SupplierQueryServiceImpl() {
        super(SUPPLIER);
    }

    @Override
    public boolean supplierWithIdExists(long supplierId) {
        return this.query().from(SUPPLIER).where(SUPPLIER.id.eq(supplierId)).fetchOne() != null;
    }
}
