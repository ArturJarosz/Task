package com.arturjarosz.task.project.status.task.validator.impl;

import com.arturjarosz.task.contract.status.validator.ContractWorkflowValidator;
import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.project.status.task.validator.TaskStatusTransitionValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class TaskStartProgressValidator implements TaskStatusTransitionValidator {
    private final ContractWorkflowValidator contractWorkflowValidator;

    @Autowired
    public TaskStartProgressValidator(ContractWorkflowValidator contractWorkflowValidator) {
        this.contractWorkflowValidator = contractWorkflowValidator;
    }

    @Override
    public void validate(Project project, Task task, Long stageId, TaskStatusTransition statusTransition) {
        this.contractWorkflowValidator.validateContractAllowsWorking(project.getId());

        ProjectStatus status = project.getStatus();
        assertIsTrue(status != ProjectStatus.REJECTED,
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS,
                        ProjectExceptionCodes.REJECTED, ProjectExceptionCodes.START_PROGRESS));
        assertIsTrue(status != ProjectStatus.DONE,
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS,
                        ProjectExceptionCodes.DONE, ProjectExceptionCodes.START_PROGRESS));
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return TaskStatusTransition.START_PROGRESS;
    }
}
