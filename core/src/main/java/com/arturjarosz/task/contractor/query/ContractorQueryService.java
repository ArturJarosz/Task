package com.arturjarosz.task.contractor.query;

import com.arturjarosz.task.finance.model.ContractorContractorJobDto;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ContractorQueryService {

    /**
     * Checks whether contractor with given contractorId exists.
     */
    boolean contractorWithIdExists(long contractorId);

    Map<Long, Long> getNumberOfJobsPerContractor();

    Set<ContractorContractorJobDto> getContractorJobsData(long contractorId);

    /**
     * Number of contractors of each of given contractor categories, 0 for a category with no contractors.
     */
    Map<Long, Long> getNumberOfContractorsByCategoryIds(Collection<Long> categoryIds);
}
