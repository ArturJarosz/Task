package com.arturjarosz.task.contract.domain;

import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.dto.ContractDto;

public interface ContractDomainService {
    Contract createContract(ContractDto contractDto);

    Contract updateContractStatus(Contract contract, ContractDto contractDto);
}
