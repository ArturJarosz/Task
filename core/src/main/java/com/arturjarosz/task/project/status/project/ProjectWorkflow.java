package com.arturjarosz.task.project.status.project;

import com.arturjarosz.task.sharedkernel.status.WorkAwareStatusWorkflow;
import com.arturjarosz.task.sharedkernel.status.Workflow;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
public class ProjectWorkflow extends Workflow<ProjectStatus> implements WorkAwareStatusWorkflow<ProjectStatus> {
    public static final String PROJECT_WORKFLOW = "ProjectWorkflow";
    private static final Set<ProjectStatus> STATUSES_FOR_CREATING_WORK_OBJECTS = Sets.newHashSet(ProjectStatus.TO_DO,
            ProjectStatus.IN_PROGRESS, ProjectStatus.DONE);
    private static final Set<ProjectStatus> STATUSES_FOR_WORKING = Sets.newHashSet(ProjectStatus.TO_DO,
            ProjectStatus.IN_PROGRESS, ProjectStatus.DONE);

    public ProjectWorkflow() {
        super(PROJECT_WORKFLOW, ProjectStatus.TO_DO, Arrays.asList(ProjectStatus.values()));
    }

    @Override
    public Set<ProjectStatus> getStatusesThatAllowCreatingWorkObjects() {
        return STATUSES_FOR_CREATING_WORK_OBJECTS;
    }

    @Override
    public Set<ProjectStatus> getStatusesThatAllowWorking() {
        return STATUSES_FOR_WORKING;
    }
}
