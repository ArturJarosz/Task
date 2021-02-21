package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class TaskValidator {

    ProjectQueryService projectQueryService;

    public TaskValidator(ProjectQueryService projectQueryService) {
        this.projectQueryService = projectQueryService;
    }

    /**
     * Validated whether TaskDto contains all data for creating Task and validate their correctness.
     *
     * @param taskDto
     */
    public static void validateCreateTaskDto(TaskDto taskDto) {
        assertNotNull(taskDto, createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.TASK));
        assertNotNull(taskDto.getName(),
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.TASK, ProjectExceptionCodes.NAME));
        assertNotEmpty(taskDto.getName(),
                createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.TASK, ProjectExceptionCodes.NAME));
        assertNotNull(taskDto.getType(),
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.TASK, ProjectExceptionCodes.TYPE));
    }

    public void validateExistenceOfTaskInStage(Long stageId, Long taskId) {
        Stage stage = this.projectQueryService.getStageById(stageId);
        Task task = stage.getTasks().stream()
                .filter(taskOnStage -> taskOnStage.getId().equals(taskId))
                .findFirst()
                .orElse(null);
        assertNotNull(task,
                createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.TASK));
    }
}
