package com.arturjarosz.task.contract.application;

import com.arturjarosz.task.contract.domain.ContractExceptionCodes;
import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.contract.status.ContractWorkflow;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class ContractWorkflowValidator {
    private final ProjectQueryService projectQueryService;
    private final ContractWorkflow contractWorkflow;

    @Autowired
    public ContractWorkflowValidator(ProjectQueryService projectQueryService, ContractWorkflow contractWorkflow) {
        this.projectQueryService = projectQueryService;
        this.contractWorkflow = contractWorkflow;
    }

    public void validateContractAllowsForWorkObjectsCreation(Long projectId) {
        ContractStatus contractStatus = this.projectQueryService.getContractStatusForProject(projectId);
        assertIsTrue(this.contractWorkflow.getStatusesThatAllowCreatingProjectObjects().contains(contractStatus),
                createMessageCode(ExceptionCodes.NOT_VALID, ContractExceptionCodes.CONTRACT,
                        ContractExceptionCodes.STATUS, ContractExceptionCodes.OBJECT, ContractExceptionCodes.CREATE),
                contractStatus.getStatusName());
    }

    public void validateContractAllowsWorking(Long projectId) {
        ContractStatus contractStatus = this.projectQueryService.getContractStatusForProject(projectId);
        assertIsTrue(this.contractWorkflow.getStatusesThatAllowWorking().contains(contractStatus),
                createMessageCode(ExceptionCodes.NOT_VALID, ContractExceptionCodes.CONTRACT,
                        ContractExceptionCodes.STATUS, ContractExceptionCodes.WORK,
                        ContractExceptionCodes.TRANSITION), contractStatus.getStatusName());
    }
}
