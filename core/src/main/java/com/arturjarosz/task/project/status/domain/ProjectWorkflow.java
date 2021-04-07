package com.arturjarosz.task.project.status.domain;

import com.arturjarosz.task.sharedkernel.status.Workflow;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ProjectWorkflow extends Workflow<ProjectStatus> {
    public static final String PROJECT_WORKFLOW = "ProjectWorkflow";

    public ProjectWorkflow() {
        super(PROJECT_WORKFLOW, ProjectStatus.OFFER, Arrays.asList(ProjectStatus.values()));
    }
}
