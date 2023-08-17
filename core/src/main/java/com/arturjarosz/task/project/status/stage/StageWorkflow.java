package com.arturjarosz.task.project.status.stage;

import com.arturjarosz.task.sharedkernel.status.WorkAwareStatusWorkflow;
import com.arturjarosz.task.sharedkernel.status.Workflow;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
public class StageWorkflow extends Workflow<StageStatus> implements WorkAwareStatusWorkflow<StageStatus> {
    public static final String STAGE_WORKFLOW = "StageWorkflow";
    private static final Set<StageStatus> STATUSES_FOR_CREATING_WORK_OBJECTS = Set.of(StageStatus.TO_DO,
            StageStatus.IN_PROGRESS, StageStatus.DONE);
    private static final Set<StageStatus> STATUSES_FOR_WORKING = Set.of(StageStatus.TO_DO,
            StageStatus.IN_PROGRESS, StageStatus.DONE);

    public StageWorkflow() {
        super(STAGE_WORKFLOW, StageStatus.TO_DO, Arrays.asList(StageStatus.values()));
    }

    @Override
    public Set<StageStatus> getStatusesThatAllowWorking() {
        return STATUSES_FOR_CREATING_WORK_OBJECTS;
    }

    @Override
    public Set<StageStatus> getStatusesThatAllowCreatingWorkObjects() {
        return STATUSES_FOR_WORKING;
    }
}
