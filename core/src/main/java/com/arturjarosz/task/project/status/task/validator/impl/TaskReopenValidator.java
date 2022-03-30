package com.arturjarosz.task.project.status.task.validator.impl;

import com.arturjarosz.task.contract.application.ContractWorkflowValidator;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.project.status.task.validator.TaskStatusTransitionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskReopenValidator implements TaskStatusTransitionValidator {
    private final ContractWorkflowValidator contractWorkflowValidator;

    @Autowired
    public TaskReopenValidator(ContractWorkflowValidator contractWorkflowValidator) {
        this.contractWorkflowValidator = contractWorkflowValidator;
    }

    @Override
    public void validate(Project project, Task task, Long stageId, TaskStatusTransition statusTransition) {
        this.contractWorkflowValidator.validateContractAllowsWorking(project.getId());
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return TaskStatusTransition.REOPEN;
    }
}
