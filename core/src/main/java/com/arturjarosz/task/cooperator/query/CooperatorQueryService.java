package com.arturjarosz.task.cooperator.query;

public interface CooperatorQueryService {

    /**
     * Checks if Cooperator with cooperatorId exists and is type of Supplier.
     * @param cooperatorId
     * @return
     */
    boolean supplierWithIdExists(Long cooperatorId);
}
