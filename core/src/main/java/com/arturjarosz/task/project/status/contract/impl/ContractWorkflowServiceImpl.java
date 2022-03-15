package com.arturjarosz.task.project.status.contract.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Contract;
import com.arturjarosz.task.project.status.contract.ContractStatus;
import com.arturjarosz.task.project.status.contract.ContractStatusTransition;
import com.arturjarosz.task.project.status.contract.ContractWorkflowService;
import com.arturjarosz.task.project.status.contract.validator.ContractStatusTransitionValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationService
public class ContractWorkflowServiceImpl implements ContractWorkflowService {
    private Map<String, List<ContractStatusTransitionValidator>> mapNameToStatusTransitionValidator;

    @Autowired
    public ContractWorkflowServiceImpl(List<ContractStatusTransitionValidator> transitionValidatorList) {
        this.mapNameToStatusTransitionValidator = new HashMap<>();
        this.mapNameToStatusTransitionValidator = transitionValidatorList.stream().collect(Collectors.groupingBy(
                contractStatusTransitionValidator -> contractStatusTransitionValidator.getStatusTransition()
                        .getName()));
    }

    @Override
    public void changeContractStatus(Contract contract, ContractStatus newStatus) {
        ContractStatus oldStatus = contract.getStatus();
        ContractStatusTransition statusTransition = this.getContractStatusTransition(oldStatus, newStatus);
        BaseValidator.assertNotNull(statusTransition,
                BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.CONTRACT,
                        ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TRANSITION),
                oldStatus != null ? oldStatus.getStatusName() : "null", newStatus.getStatusName());
        this.beforeStatusChange(contract, statusTransition);
        this.changeStatus(contract, newStatus);
        this.afterStatusChange(contract, statusTransition);

    }

    @Override
    public void beforeStatusChange(Contract contract, ContractStatusTransition statusTransition) {
        List<ContractStatusTransitionValidator> validators = this.getStatusTransitionValidators(statusTransition);
        validators.forEach(validator -> validator.validate(contract, statusTransition));
    }

    @Override
    public void afterStatusChange(Contract contract, ContractStatusTransition statusTransition) {
        // TODO: define status change listeners
    }

    @Override
    public void changeStatus(Contract contract, ContractStatus status) {
        contract.changeStatus(status);
    }

    private ContractStatusTransition getContractStatusTransition(ContractStatus oldStaus, ContractStatus newStatus) {
        return Arrays.stream(ContractStatusTransition.values())
                .filter(statusTransition -> statusTransition.getCurrentStatus() == oldStaus && statusTransition.getNextStatus() == newStatus)
                .findFirst().orElse(null);
    }

    private List<ContractStatusTransitionValidator> getStatusTransitionValidators(
            ContractStatusTransition statusTransition) {
        List<ContractStatusTransitionValidator> validators = this.mapNameToStatusTransitionValidator.get(
                statusTransition.getName());
        if (validators == null) {
            return Collections.emptyList();
        }
        return validators;
    }
}
