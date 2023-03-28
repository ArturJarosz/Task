package com.arturjarosz.task.contract.status.validator;

import com.arturjarosz.task.contract.domain.ContractExceptionCodes;
import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.contract.status.ContractStatusWorkflow;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@RequiredArgsConstructor
@Component
public class ContractWorkflowValidator {
    @NonNull
    private final ProjectQueryService projectQueryService;
    @NonNull
    private final ContractStatusWorkflow contractWorkflow;

    public void validateContractAllowsForWorkObjectsCreation(Long projectId) {
        ContractStatus contractStatus = this.projectQueryService.getContractStatusForProject(projectId);
        assertIsTrue(this.contractWorkflow.getStatusesThatAllowCreatingWorkObjects().contains(contractStatus),
                createMessageCode(ExceptionCodes.NOT_VALID, ContractExceptionCodes.CONTRACT,
                        ContractExceptionCodes.STATUS, ContractExceptionCodes.OBJECT, ContractExceptionCodes.CREATE),
                contractStatus.getStatusName());
    }

    public void validateContractAllowsWorking(Long projectId) {
        ContractStatus contractStatus = this.projectQueryService.getContractStatusForProject(projectId);
        assertIsTrue(this.contractWorkflow.getStatusesThatAllowWorking().contains(contractStatus),
                createMessageCode(ExceptionCodes.NOT_VALID, ContractExceptionCodes.CONTRACT,
                        ContractExceptionCodes.STATUS, ContractExceptionCodes.WORK, ContractExceptionCodes.TRANSITION),
                contractStatus.getStatusName());
    }
}
