package com.arturjarosz.task.project.status.contract.impl;

import com.arturjarosz.task.project.model.Contract;
import com.arturjarosz.task.project.status.contract.ContractStatus;
import com.arturjarosz.task.project.status.contract.ContractStatusTransitionService;
import com.arturjarosz.task.project.status.contract.ContractWorkflowService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;

@ApplicationService
public class ContractStatusTransitionServiceImpl implements ContractStatusTransitionService {
    private final ContractWorkflowService contractWorkflowService;

    @Autowired
    public ContractStatusTransitionServiceImpl(ContractWorkflowService contractWorkflowService) {
        this.contractWorkflowService = contractWorkflowService;
    }

    @Override
    public void create(Contract contract) {
        this.contractWorkflowService.changeContractStatus(contract, ContractStatus.OFFER);
    }

    @Override
    public void rejectOffer(Contract contract) {
        this.contractWorkflowService.changeContractStatus(contract, ContractStatus.REJECTED);
    }

    @Override
    public void makeNewOffer(Contract contract) {
        this.contractWorkflowService.changeContractStatus(contract, ContractStatus.OFFER);
    }

    @Override
    public void acceptOffer(Contract contract) {
        this.contractWorkflowService.changeContractStatus(contract, ContractStatus.ACCEPTED);
    }

    @Override
    public void rejectAcceptedOffer(Contract contract) {
        this.contractWorkflowService.changeContractStatus(contract, ContractStatus.REJECTED);
    }

    @Override
    public void signContract(Contract contract) {
        this.contractWorkflowService.changeContractStatus(contract, ContractStatus.SIGNED);
    }

    @Override
    public void terminateContract(Contract contract) {
        this.contractWorkflowService.changeContractStatus(contract, ContractStatus.TERMINATED);
    }

    @Override
    public void resumeContract(Contract contract) {
        this.contractWorkflowService.changeContractStatus(contract, ContractStatus.SIGNED);
    }

    @Override
    public void completeContract(Contract contract) {
        this.contractWorkflowService.changeContractStatus(contract, ContractStatus.COMPLETED);
    }
}
