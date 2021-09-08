package com.arturjarosz.task.stage.application;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.stage.application.dto.StageDto;
import com.arturjarosz.task.stage.model.Stage;
import com.arturjarosz.task.stage.query.StageQueryService;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.*;

@Component
public class StageValidator {

    private final ProjectRepository projectRepository;
    private final StageQueryService stageQueryService;

    public StageValidator(ProjectRepository projectRepository, StageQueryService stageQueryService) {
        this.projectRepository = projectRepository;
        this.stageQueryService = stageQueryService;
    }

    /**
     * Validates whether stageDto contains all needed data for creating stage, and validates their correctness.
     *
     * @param stageDto
     */
    public void validateCreateStageDto(StageDto stageDto) {
        this.validateBasicDate(stageDto);
    }

    /**
     * Validates whether stageDto contains all needed data for updating Stage, and validates their correctness.
     *
     * @param stageDto
     */
    public void validateUpdateStageDto(StageDto stageDto) {
        this.validateBasicDate(stageDto);
    }

    /**
     * Validates if Stage of given stageId exist.
     *
     * @param stageId
     */
    public void validateExistenceOfStageInProject(Long projectId, Long stageId) {
        Stage stage = this.projectRepository.load(projectId).getStages().stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .findFirst()
                .orElse(null);
        assertNotNull(stage, createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.PROJECT,
                ProjectExceptionCodes.STAGE));
    }

    /**
     * Validates basic data that are needed on stage creation and update.
     *
     * @param stageDto
     */
    private void validateBasicDate(StageDto stageDto) {
        assertNotNull(stageDto, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.STAGE));
        assertNotNull(stageDto.getName(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.NAME));
        assertNotEmpty(stageDto.getName(),
                createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.NAME));
        assertNotNull(stageDto.getStageType(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.TYPE));
    }

    /**
     * Checks if Installment is not set on Stage yet.
     *
     * @param stageId
     */
    public void validateStageNotHavingInstallment(Long stageId) {
        Stage stage = this.stageQueryService.getStageById(stageId);
        assertIsTrue(stage.getInstallment() == null,
                createMessageCode(ExceptionCodes.ALREADY_SET, ProjectExceptionCodes.STAGE,
                        ProjectExceptionCodes.INSTALLMENT));
    }

    public void validateStageHavingInstallment(Long stageId) {
        Stage stage = this.stageQueryService.getStageById(stageId);
        assertIsTrue(stage.getInstallment() != null,
                createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.STAGE,
                        ProjectExceptionCodes.INSTALLMENT));
    }
}
