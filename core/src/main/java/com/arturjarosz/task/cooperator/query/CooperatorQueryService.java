package com.arturjarosz.task.cooperator.query;

public interface CooperatorQueryService {

    /**
     * Checks if Supplier with supplierId exists.
     * @param supplierId
     * @return
     */
    boolean supplierWithIdExists(Long supplierId);

    /**
     * Checks if Contractor with contractorId exists.
     * @param contractorId
     * @return
     */
    boolean contractorWithIdExists(Long contractorId);
}
