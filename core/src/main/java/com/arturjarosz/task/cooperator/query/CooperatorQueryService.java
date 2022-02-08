package com.arturjarosz.task.cooperator.query;

public interface CooperatorQueryService {

    /**
     * Checks if Supplier with supplierId exists.
     */
    boolean supplierWithIdExists(Long supplierId);

    /**
     * Checks if Contractor with contractorId exists.
     */
    boolean contractorWithIdExists(Long contractorId);
}
