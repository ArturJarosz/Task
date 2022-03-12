package com.arturjarosz.task.project.status.Contract;

import com.arturjarosz.task.project.model.Contract;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface ContractWorkflowService extends WorkflowService<ContractStatus, Contract> {

    void changeContractStatus(Contract contract, ContractStatus newStatus);

    void beforeStatusChange(Contract contract, ContractStatusTransition statusTransition);

    void afterStatusChange(Contract contract, ContractStatusTransition statusTransition);
}
