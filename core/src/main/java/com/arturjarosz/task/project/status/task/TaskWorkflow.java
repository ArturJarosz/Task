package com.arturjarosz.task.project.status.task;

import com.arturjarosz.task.sharedkernel.status.Workflow;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class TaskWorkflow extends Workflow<TaskStatus> {
    public static final String TASK_WORKFLOW = "TaskWorkflow";

    public TaskWorkflow() {
        super(TASK_WORKFLOW, TaskStatus.TO_DO, Arrays.asList(TaskStatus.values()));
    }
}
