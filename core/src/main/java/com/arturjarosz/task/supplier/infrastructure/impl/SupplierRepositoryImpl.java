package com.arturjarosz.task.supplier.infrastructure.impl;

import com.arturjarosz.task.sharedkernel.infrastructure.impl.GenericJpaRepositoryImpl;
import com.arturjarosz.task.supplier.infrastructure.SupplierRepository;
import com.arturjarosz.task.supplier.model.QSupplier;
import com.arturjarosz.task.supplier.model.Supplier;
import org.springframework.stereotype.Repository;

@Repository
public class SupplierRepositoryImpl extends GenericJpaRepositoryImpl<Supplier, QSupplier> implements SupplierRepository {
    private static final QSupplier SUPPLIER = QSupplier.supplier;

    public SupplierRepositoryImpl(){
        super(SUPPLIER);
    }
}
