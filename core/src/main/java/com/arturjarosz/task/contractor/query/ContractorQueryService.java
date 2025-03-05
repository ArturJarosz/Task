package com.arturjarosz.task.contractor.query;

import com.arturjarosz.task.finance.model.ContractorContractorJobDto;

import java.util.Map;
import java.util.Set;

public interface ContractorQueryService {

    /**
     * Checks whether contractor with given contractorId exists.
     */
    boolean contractorWithIdExists(long contractorId);

    Map<Long, Long> getNumberOfJobsPerContractor();

    Set<ContractorContractorJobDto> getContractorJobsData(long contractorId);
}
