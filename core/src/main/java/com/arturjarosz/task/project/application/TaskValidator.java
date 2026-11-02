package com.arturjarosz.task.project.application;

import com.arturjarosz.task.architect.application.ArchitectValidator;
import com.arturjarosz.task.dto.TaskDto;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class TaskValidator {

    private final ProjectQueryService projectQueryService;
    private final ArchitectValidator architectValidator;
    private final TaskTypeValidator taskTypeValidator;

    @Autowired
    public TaskValidator(ProjectQueryService projectQueryService, ArchitectValidator architectValidator,
            TaskTypeValidator taskTypeValidator) {
        this.projectQueryService = projectQueryService;
        this.architectValidator = architectValidator;
        this.taskTypeValidator = taskTypeValidator;
    }

    /**
     * Validated whether TaskDto contains all data for creating Task and validate their correctness.
     */
    public void validateCreateTaskDto(TaskDto taskDto) {
        assertNotNull(taskDto, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.TASK));
        this.validateName(taskDto.getName());
        assertNotNull(taskDto.getTypeId(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.TASK, ProjectExceptionCodes.TYPE));
        this.taskTypeValidator.validateExistence(taskDto.getTypeId());
        assertNotNull(taskDto.getArchitectId(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.TASK, ProjectExceptionCodes.ARCHITECT));
        this.architectValidator.validateArchitectExistence(taskDto.getArchitectId());
    }

    public void validateExistenceOfTaskInStage(Long stageId, Long taskId) {
        var stage = this.projectQueryService.getStageById(stageId);
        assertEntityNotNull(stage.getTasks(),
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.TASK),
                stageId, taskId);
        var task = stage.getTasks()
                .stream()
                .filter(taskOnStage -> taskOnStage.getId().equals(taskId))
                .findFirst()
                .orElse(null);
        assertEntityNotNull(task,
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.TASK),
                stageId, taskId);
    }

    public void validateUpdateTaskDto(TaskDto taskDto) {
        this.validateCreateTaskDto(taskDto);
    }

    private void validateName(String name) {
        assertNotNull(name,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.TASK, ProjectExceptionCodes.NAME));
        assertNotEmpty(name,
                createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.TASK, ProjectExceptionCodes.NAME));
    }
}
