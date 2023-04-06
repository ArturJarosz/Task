package com.arturjarosz.task.project.status.project.validator;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.*;

@Component
public class ProjectWorkflowValidator {
    private final List<ProjectWorkflow> projectWorkflows;

    @Autowired
    public ProjectWorkflowValidator(List<ProjectWorkflow> projectWorkflows) {
        this.projectWorkflows = projectWorkflows;
    }

    public void validateProjectAllowsForWorkObjectCreation(Project project) {
        ProjectWorkflow projectWorkflow = this.projectWorkflows.stream()
                .filter(workflow -> workflow.getName().equals(project.getWorkflowName())).findFirst()
                .orElseThrow(ResourceNotFoundException::new);
        assertNotNull(projectWorkflow, createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT,
                ProjectExceptionCodes.WORKFLOW));
        assertIsTrue(projectWorkflow.getStatusesThatAllowCreatingWorkObjects()
                        .contains(project.getStatus()),
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS,
                        ProjectExceptionCodes.CREATE));
    }

    public void validateProjectAllowsForWorking(Project project) {
        ProjectWorkflow projectWorkflow = this.projectWorkflows.stream()
                .filter(workflow -> workflow.getName().equals(project.getWorkflowName())).findFirst()
                .orElseThrow(ResourceNotFoundException::new);
        assertNotNull(projectWorkflow, createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT,
                ProjectExceptionCodes.WORKFLOW));
        assertIsTrue(projectWorkflow.getStatusesThatAllowWorking()
                        .contains(project.getStatus()),
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS,
                        ProjectExceptionCodes.WORK), project.getStatus().getStatusName());
    }
}
