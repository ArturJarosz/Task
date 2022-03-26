package com.arturjarosz.task.contract.application.status.impl;

import com.arturjarosz.task.contract.application.status.ContractStatus;
import com.arturjarosz.task.contract.application.status.ContractStatusTransitionService;
import com.arturjarosz.task.contract.application.status.ContractWorkflowService;
import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import org.springframework.beans.factory.annotation.Autowired;

@ApplicationService
public class ContractStatusTransitionServiceImpl implements ContractStatusTransitionService {
    private final ContractWorkflowService contractWorkflowService;

    @Autowired
    public ContractStatusTransitionServiceImpl(ContractWorkflowService contractWorkflowService) {
        this.contractWorkflowService = contractWorkflowService;
    }

    @Override
    public void createOffer(Contract contract) {
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

    @Override
    public void reject(Contract contract) {
        if (ContractStatus.OFFER.equals(contract.getStatus())) {
            this.rejectOffer(contract);
        } else if (ContractStatus.ACCEPTED.equals(contract.getStatus())) {
            this.rejectAcceptedOffer(contract);
        } else {
            throw new IllegalArgumentException(
                    BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.CONTRACT,
                            ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TRANSITION),
                    contract.getStatus() != null ? contract.getStatus().getStatusName() : "null",
                    ContractStatus.REJECTED.getStatusName());
        }
    }
}
