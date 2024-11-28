package com.arturjarosz.task.supplier.query;

import com.arturjarosz.task.finance.model.SupplierSupplyDataDto;

import java.util.Set;

public interface SupplierQueryService {

    /**
     * Checks whether supplier with given supplierId exists.
     */
    boolean supplierWithIdExists(long supplierId);

    Set<SupplierSupplyDataDto> getSupplierSuppliesData(long supplierId);
}
