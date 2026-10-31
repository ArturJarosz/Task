package com.arturjarosz.task.contract.application;

import com.arturjarosz.task.dto.ContractDto;

import java.util.Collection;
import java.util.Map;

public interface ContractService {

    ContractDto createContract(ContractDto contractDto);

    ContractDto changeStatus(Long contractId, ContractDto contractDto);

    ContractDto getContractForProject(Long projectId);

    /**
     * Contracts with the given ids, by id, loaded in one query. Ids without a contract are left out.
     */
    Map<Long, ContractDto> getContractsByIds(Collection<Long> contractIds);

    ContractDto updateContract(Long contractId, ContractDto contractDto);
}
