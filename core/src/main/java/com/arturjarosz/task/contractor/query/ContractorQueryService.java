package com.arturjarosz.task.contractor.query;

public interface ContractorQueryService {

    /**
     *  Checks whether contractor with given contractorId exists.
     */
    boolean contractorWithIdExists(long contractorId);
}
