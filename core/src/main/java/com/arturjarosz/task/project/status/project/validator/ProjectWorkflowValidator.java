package com.arturjarosz.task.project.status.project.validator;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.domain.dto.ProjectStatusData;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class ProjectWorkflowValidator {
    private final ProjectQueryService projectQueryService;
    private final List<ProjectWorkflow> projectWorkflows;

    @Autowired
    public ProjectWorkflowValidator(ProjectQueryService projectQueryService, List<ProjectWorkflow> projectWorkflows) {
        this.projectQueryService = projectQueryService;
        this.projectWorkflows = projectWorkflows;
    }

    public void validateProjectAllowsForWorkObjectCreation(Long projectId) {
        ProjectStatusData projectStatusData = this.projectQueryService.getProjectStatusData(projectId);
        ProjectWorkflow projectWorkflow = this.projectWorkflows.stream()
                .filter(workflow -> workflow.getName().equals(projectStatusData.getWorkflowName())).findFirst()
                .orElse(null);
        assertNotNull(projectWorkflow, createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT,
                ProjectExceptionCodes.WORKFLOW));
        assertIsTrue(projectWorkflow.getStatusesThatAllowCreatingWorkObjects()
                        .contains(projectStatusData.getProjectStatus()),
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS,
                        ProjectExceptionCodes.CREATE));
    }

    public void validateProjectAllowsForWorking(Long projectId){
        ProjectStatusData projectStatusData = this.projectQueryService.getProjectStatusData(projectId);
        ProjectWorkflow projectWorkflow = this.projectWorkflows.stream()
                .filter(workflow -> workflow.getName().equals(projectStatusData.getWorkflowName())).findFirst()
                .orElse(null);
        assertNotNull(projectWorkflow, createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT,
                ProjectExceptionCodes.WORKFLOW));
        assertIsTrue(projectWorkflow.getStatusesThatAllowWorking()
                        .contains(projectStatusData.getProjectStatus()),
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS,
                        ProjectExceptionCodes.WORK));
    }

    public void validateProjectAllowsForWorkObjectCreation(Project project) {
        ProjectWorkflow projectWorkflow = this.projectWorkflows.stream()
                .filter(workflow -> workflow.getName().equals(project.getWorkflowName())).findFirst()
                .orElse(null);
        assertNotNull(projectWorkflow, createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT,
                ProjectExceptionCodes.WORKFLOW));
        assertIsTrue(projectWorkflow.getStatusesThatAllowCreatingWorkObjects()
                        .contains(project.getStatus()),
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS,
                        ProjectExceptionCodes.CREATE));
    }

    public void validateProjectAllowsForWorking(Project project){
        ProjectWorkflow projectWorkflow = this.projectWorkflows.stream()
                .filter(workflow -> workflow.getName().equals(project.getWorkflowName())).findFirst()
                .orElse(null);
        assertNotNull(projectWorkflow, createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT,
                ProjectExceptionCodes.WORKFLOW));
        assertIsTrue(projectWorkflow.getStatusesThatAllowWorking()
                        .contains(project.getStatus()),
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS,
                        ProjectExceptionCodes.WORK), project.getStatus().getStatusName());
    }
}
