package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.CostDto;
import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

public class CostValidator {

    public static void validateCostDto(CostDto costDto) {
        assertNotNull(costDto,
                BaseValidator.createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.COST));
        assertNotNull(costDto.getCategory(), BaseValidator
                .createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.COST, ProjectExceptionCodes.CATEGORY));
        assertNotNull(costDto.getDate(),
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.COST, ProjectExceptionCodes.COST_DATE));
        assertIsTrue(costDto.getValue() >= 0, createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.COST,
                ProjectExceptionCodes.NEGATIVE));
        validateCostName(costDto.getName());
    }

    public static void validateCostExistence(Cost cost, Long costId) {
        assertNotNull(cost,
                BaseValidator.createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.COST), costId);
    }

    private static void validateCostName(String costName) {
        assertNotNull(costName,
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.COST, ProjectExceptionCodes.NAME));
        assertNotEmpty(costName,
                createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.COST, ProjectExceptionCodes.NAME));
    }
}
