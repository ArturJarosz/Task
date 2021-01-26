package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class StageValidator {

    ProjectQueryService projectQueryService;

    public StageValidator(ProjectQueryService projectQueryService) {
        this.projectQueryService = projectQueryService;
    }

    /**
     * Validates whether stageDto contains all needed data for creating stage, and validates their correctness.
     *
     * @param stageDto
     */
    public static void validateCreateStageDto(StageDto stageDto) {
        validateBasicDate(stageDto);
    }

    /**
     * Validates whether stageDto contains all needed data for updating Stage, and validates their correctness.
     *
     * @param stageDto
     */
    public static void validateUpdateStageDto(StageDto stageDto) {
        validateBasicDate(stageDto);
    }

    /**
     * Validates if Stage of given stageId exist.
     *
     * @param stageId
     */
    public void validateStageExistence(Long stageId) {
        assertNotNull(this.projectQueryService.getStageById(stageId),
                createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.STAGE));
    }

    /**
     * Validates basic data that are needed on stage creation and update.
     *
     * @param stageDto
     */
    private static void validateBasicDate(StageDto stageDto) {
        assertNotNull(stageDto, createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.STAGE));
        assertNotNull(stageDto.getName(),
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.NAME));
        assertNotEmpty(stageDto.getName(),
                createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.NAME));
        assertNotNull(stageDto.getStageType(),
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.STAGE, ProjectExceptionCodes.TYPE));
    }

    /**
     * Checks if Installment is not set on Stage yet.
     *
     * @param stageId
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
                createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.STAGE,
                        ProjectExceptionCodes.INSTALLMENT));
    }
}
