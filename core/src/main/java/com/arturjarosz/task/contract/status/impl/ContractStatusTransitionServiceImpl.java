package com.arturjarosz.task.contract.status.impl;

import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.contract.status.ContractStatusTransitionService;
import com.arturjarosz.task.contract.status.ContractWorkflowService;
import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationService
public class ContractStatusTransitionServiceImpl implements ContractStatusTransitionService {
    @NonNull
    private final ContractWorkflowService contractWorkflowService;

    @Override
    public void createOffer(Contract contract) {
        this.contractWorkflowService.changeContractStatus(contract, ContractStatus.OFFER);
    }

    @Override
    public void rejectOffer(Contract contract) {
        this.contractWorkflowService.changeContractStatus(contract, ContractStatus.REJECTED);
    }

    @Override
    public void rejectAcceptedOffer(Contract contract) {
        this.contractWorkflowService.changeContractStatus(contract, ContractStatus.REJECTED);
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

    @Override
    public void changeStatus(Contract contract, ContractStatus newStatus) {
        if (ContractStatus.REJECTED == newStatus) {
            this.reject(contract);
        } else {
            this.contractWorkflowService.changeContractStatus(contract, newStatus);
        }
    }
}
