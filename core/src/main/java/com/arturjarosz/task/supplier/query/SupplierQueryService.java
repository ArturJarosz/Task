package com.arturjarosz.task.supplier.query;

public interface SupplierQueryService {

    /**
     * Checks whether supplier with given supplierId exists.
     */
    boolean supplierWithIdExists(long supplierId);
}
