package com.arturjarosz.task.stage.status;

import com.arturjarosz.task.sharedkernel.status.Workflow;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StageWorkflow extends Workflow<StageStatus> {
    public static final String STAGE_WORKFLOW = "StageWorkflow";

    public StageWorkflow() {
        super(STAGE_WORKFLOW, StageStatus.TO_DO, Arrays.asList(StageStatus.values()));
    }
}
