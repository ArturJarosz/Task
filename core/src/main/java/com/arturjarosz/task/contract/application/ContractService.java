package com.arturjarosz.task.contract.application;

import com.arturjarosz.task.dto.ContractDto;

public interface ContractService {

    ContractDto createContract(ContractDto contractDto);

    ContractDto changeStatus(Long contractId, ContractDto contractDto);

    ContractDto getContractForProject(Long projectId);

    ContractDto updateContract(Long contractId, ContractDto contractDto);
}
