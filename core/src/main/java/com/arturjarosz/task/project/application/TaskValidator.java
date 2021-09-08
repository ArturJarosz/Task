package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.stage.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.stage.query.StageQueryService;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class TaskValidator {

    StageQueryService stageQueryService;

    public TaskValidator(StageQueryService stageQueryService) {
        this.stageQueryService = stageQueryService;
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
        Stage stage = this.stageQueryService.getStageById(stageId);
        assertNotNull(stage.getTasks(),
                createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.TASK));
        Task task = stage.getTasks().stream()
                .filter(taskOnStage -> taskOnStage.getId().equals(taskId))
                .findFirst()
                .orElse(null);
        assertNotNull(task,
                createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.TASK));
    }
}
