package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.infrastructure.repositor.TaskTypeRepository;
import com.arturjarosz.task.project.model.TaskType;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryValidator;
import org.springframework.stereotype.Component;

/**
 * Validates task type dtos and TaskType entities.
 */
@Component
public class TaskTypeValidator extends NamedEntryValidator<TaskType> {

    public TaskTypeValidator(TaskTypeRepository taskTypeRepository, ProjectQueryService projectQueryService) {
        super(taskTypeRepository, ProjectExceptionCodes.TASK_TYPE, ProjectExceptionCodes.TASKS, projectQueryService::getNumberOfTasksByTaskTypeIds);
    }
}
