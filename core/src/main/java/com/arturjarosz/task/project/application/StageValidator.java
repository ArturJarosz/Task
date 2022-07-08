package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class StageValidator {

    private final ProjectRepository projectRepository;
    private final ProjectQueryService projectQueryService;

    @Autowired
    public StageValidator(ProjectRepository projectRepository, ProjectQueryService projectQueryService) {
        this.projectRepository = projectRepository;
        this.projectQueryService = projectQueryService;
    }

    /**
     * Validates whether stageDto contains all needed data for creating stage, and validates their correctness.
     */
    public void validateCreateStageDto(StageDto stageDto) {
        this.validateBasicDate(stageDto);
    }

    /**
     * Validates whether stageDto contains all needed data for updating Stage, and validates their correctness.
     */
    public void validateUpdateStageDto(StageDto stageDto) {
        this.validateBasicDate(stageDto);
    }

    /**
     * Validates if Stage of given stageId exist.
     */
    public void validateExistenceOfStageInProject(Long projectId, Long stageId) {
        Stage stage = this.projectRepository.load(projectId).getStages().stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId)).findFirst().orElse(null);
        assertNotNull(stage,
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STAGE),
                stageId);
    }

    /**
     * Validates basic data that are needed on stage creation and update.
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
     */
    public void validateStageNotHavingInstallment(Long stageId) {
        Stage stage = this.projectQueryService.getStageById(stageId);
        assertIsTrue(stage.getInstallment() == null,
                createMessageCode(ExceptionCodes.ALREADY_SET, ProjectExceptionCodes.STAGE,
                        ProjectExceptionCodes.INSTALLMENT));
    }

    public void validateStageHavingInstallment(Long stageId) {
        Stage stage = this.projectQueryService.getStageById(stageId);
        assertIsTrue(stage.getInstallment() != null,
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.STAGE,
                        ProjectExceptionCodes.INSTALLMENT));
    }
}
