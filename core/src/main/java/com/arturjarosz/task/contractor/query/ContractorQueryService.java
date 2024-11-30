package com.arturjarosz.task.contractor.query;

import java.util.Map;

public interface ContractorQueryService {

    /**
     * Checks whether contractor with given contractorId exists.
     */
    boolean contractorWithIdExists(long contractorId);

    Map<Long, Long> getNumberOfJobsPerContractor();
}
