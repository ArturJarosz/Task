package com.arturjarosz.task.finance.application.validator;

import com.arturjarosz.task.dto.CostDto;
import com.arturjarosz.task.finance.model.Cost;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@RequiredArgsConstructor
@Component
public class CostValidator {

    @NonNull
    private final FinancialDataQueryService financialDataQueryService;

    public void validateCostDto(CostDto costDto) {
        assertNotNull(costDto, BaseValidator.createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.COST));
        assertNotNull(costDto.getCategory(),
                BaseValidator.createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.COST,
                        ProjectExceptionCodes.CATEGORY));
        assertNotNull(costDto.getDate(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.COST, ProjectExceptionCodes.COST_DATE));
        this.validateCostValue(costDto.getValue());
        this.validateCostName(costDto.getName());
    }

    public void validateCostExistence(Cost cost, Long costId) {
        assertNotNull(cost, BaseValidator.createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.COST),
                costId);
    }

    private void validateCostName(String costName) {
        assertNotNull(costName,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.COST, ProjectExceptionCodes.NAME));
        assertNotEmpty(costName,
                createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.COST, ProjectExceptionCodes.NAME));
    }

    public void validateUpdateCostDto(CostDto costDto) {
        this.validateCostDto(costDto);
    }

    public void validateCostExistence(Long costId) {
        assertIsTrue(this.financialDataQueryService.doesCostExistByCostId(costId),
                BaseValidator.createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.COST), costId);
    }

    private void validateCostValue(BigDecimal value) {
        assertIsTrue(value.doubleValue() >= 0, createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.COST,
                ProjectExceptionCodes.NEGATIVE));
    }
}
