package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.*;

@Component
public class TaskValidator {

    private final ProjectQueryService projectQueryService;

    public TaskValidator(ProjectQueryService projectQueryService) {
        this.projectQueryService = projectQueryService;
    }

    /**
     * Validated whether TaskDto contains all data for creating Task and validate their correctness.
     *
     * @param taskDto
     */
    public void validateCreateTaskDto(TaskDto taskDto) {
        assertNotNull(taskDto, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.TASK));
        assertNotNull(taskDto.getName(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.TASK, ProjectExceptionCodes.NAME));
        assertNotEmpty(taskDto.getName(),
                createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.TASK, ProjectExceptionCodes.NAME));
        assertNotNull(taskDto.getType(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.TASK, ProjectExceptionCodes.TYPE));
    }

    public void validateExistenceOfTaskInStage(Long stageId, Long taskId) {
        Stage stage = this.projectQueryService.getStageById(stageId);
        assertNotNull(stage.getTasks(),
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.TASK), stageId, taskId);
        Task task = stage.getTasks().stream()
                .filter(taskOnStage -> taskOnStage.getId().equals(taskId))
                .findFirst()
                .orElse(null);
        assertNotNull(task,
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.TASK), stageId, taskId);
    }
}
