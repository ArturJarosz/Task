package com.arturjarosz.task.contract.application.status;

import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface ContractWorkflowService extends WorkflowService<ContractStatus, Contract> {

    void changeContractStatus(Contract contract, ContractStatus newStatus);

    void beforeStatusChange(Contract contract, ContractStatusTransition statusTransition);

    void afterStatusChange(Contract contract, ContractStatusTransition statusTransition);
}
