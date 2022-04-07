package com.arturjarosz.task.project.status.stage.validator;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.stage.StageWorkflow;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class StageWorkflowValidator {
    private final List<StageWorkflow> stageWorkflows;

    @Autowired
    public StageWorkflowValidator(List<StageWorkflow> stageWorkflows) {
        this.stageWorkflows = stageWorkflows;
    }

    public void stageStatusAllowsForWorking(Stage stage) {
        StageWorkflow stageWorkflow = this.stageWorkflows.stream()
                .filter(workflow -> workflow.getName().equals(stage.getWorkflowName())).findFirst().orElse(null);
        assertNotNull(stageWorkflow, createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.STAGE,
                ProjectExceptionCodes.WORKFLOW));
        assertIsTrue(stageWorkflow.getStatusesThatAllowWorking()
                        .contains(stage.getStatus()),
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.STATUS,
                        ProjectExceptionCodes.WORK), stage.getStatus().getStatusName());
    }
}
