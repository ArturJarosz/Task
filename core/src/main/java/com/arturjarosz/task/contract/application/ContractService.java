package com.arturjarosz.task.contract.application;

import com.arturjarosz.task.dto.ContractDto;

public interface ContractService {

    ContractDto createContract(ContractDto contractDto);

    ContractDto reject(Long contractId);

    ContractDto makeNewOffer(Long contractId, ContractDto contractDto);

    ContractDto acceptOffer(Long contractId);

    ContractDto sign(Long contractId, ContractDto contractDto);

    ContractDto terminate(Long contractId, ContractDto contractDto);

    ContractDto resume(Long contractId);

    ContractDto complete(Long contractId, ContractDto contractDto);

    ContractDto getContractForProject(Long projectId);
}
