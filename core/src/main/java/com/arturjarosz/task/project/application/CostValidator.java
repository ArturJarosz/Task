package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.CostDto;
import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class CostValidator {

    private ProjectQueryService projectQueryService;

    public CostValidator(ProjectQueryService projectQueryService) {
        this.projectQueryService = projectQueryService;
    }

    public void validateCostDto(CostDto costDto) {
        assertNotNull(costDto,
                BaseValidator.createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.COST));
        assertNotNull(costDto.getCategory(), BaseValidator
                .createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.COST, ProjectExceptionCodes.CATEGORY));
        assertNotNull(costDto.getDate(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.COST, ProjectExceptionCodes.COST_DATE));
        assertIsTrue(costDto.getValue() >= 0, createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.COST,
                ProjectExceptionCodes.NEGATIVE));
        this.validateCostName(costDto.getName());
    }

    public void validateCostExistence(Cost cost, Long costId) {
        assertNotNull(cost,
                BaseValidator.createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.COST), costId);
    }

    private void validateCostName(String costName) {
        assertNotNull(costName,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.COST, ProjectExceptionCodes.NAME));
        assertNotEmpty(costName,
                createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.COST, ProjectExceptionCodes.NAME));
    }

    public void validateUpdateCostDto(CostDto costDto) {
        //TODO: what data should be updatable on Cost?
    }

    public void validateCostExistence(Long costId) {
        Cost cost = this.projectQueryService.getCostById(costId);
        this.validateCostExistence(cost, costId);
    }
}
