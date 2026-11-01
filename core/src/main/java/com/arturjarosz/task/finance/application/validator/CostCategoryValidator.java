package com.arturjarosz.task.finance.application.validator;

import com.arturjarosz.task.dto.CostCategoryDto;
import com.arturjarosz.task.finance.infrastructure.CostCategoryRepository;
import com.arturjarosz.task.finance.model.CostCategory;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsFalse;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates cost category dtos and CostCategory entities.
 */
@Component
@RequiredArgsConstructor
public class CostCategoryValidator {

    public static final String COST_CATEGORY_WITH_COSTS_MESSAGE_CODE = createMessageCode(ExceptionCodes.NOT_VALID,
            ProjectExceptionCodes.COST_CATEGORY, ProjectExceptionCodes.COSTS);
    public static final String COST_CATEGORY_NAME_UNIQUE_MESSAGE_CODE = createMessageCode(ExceptionCodes.NOT_VALID,
            ProjectExceptionCodes.COST_CATEGORY, ProjectExceptionCodes.NAME, ProjectExceptionCodes.UNIQUE);

    private final CostCategoryRepository costCategoryRepository;
    private final FinancialDataQueryService financialDataQueryService;

    public void validateCreateCostCategoryDto(CostCategoryDto costCategoryDto) {
        this.validateCostCategoryDto(costCategoryDto, null);
    }

    public void validateUpdateCostCategoryDto(CostCategoryDto costCategoryDto, Long costCategoryId) {
        this.validateCostCategoryDto(costCategoryDto, costCategoryId);
    }

    /**
     * Validates the name of a new Cost category (costCategoryId null) or of the renamed one with
     * costCategoryId. Names are compared without surrounding whitespace and regardless of letter case.
     */
    private void validateCostCategoryDto(CostCategoryDto costCategoryDto, Long costCategoryId) {
        assertNotNull(costCategoryDto, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.COST_CATEGORY));
        assertNotNull(costCategoryDto.getName(), createMessageCode(ExceptionCodes.NULL,
                ProjectExceptionCodes.COST_CATEGORY, ProjectExceptionCodes.NAME));
        var name = costCategoryDto.getName().strip();
        assertNotEmpty(name, createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.COST_CATEGORY,
                ProjectExceptionCodes.NAME));
        assertIsTrue(name.length() <= CostCategory.NAME_MAX_LENGTH,
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.COST_CATEGORY,
                        ProjectExceptionCodes.NAME, ProjectExceptionCodes.TOO_LONG), CostCategory.NAME_MAX_LENGTH);
        var nameTaken = costCategoryId == null
                ? this.costCategoryRepository.existsByNameIgnoreCase(name)
                : this.costCategoryRepository.existsByNameIgnoreCaseAndIdNot(name, costCategoryId);
        assertIsFalse(nameTaken, COST_CATEGORY_NAME_UNIQUE_MESSAGE_CODE, name);
    }

    public void validateCostCategoryExistence(Optional<CostCategory> maybeCostCategory, Long costCategoryId) {
        assertEntityPresent(maybeCostCategory.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.COST_CATEGORY), costCategoryId);
    }

    public void validateCostCategoryExistence(Long costCategoryId) {
        this.validateCostCategoryExistence(this.costCategoryRepository.findById(costCategoryId), costCategoryId);
    }

    public void validateCostCategoryHasNoCosts(Long costCategoryId) {
        var costsCount = this.financialDataQueryService.getNumberOfCostsByCategoryIds(List.of(costCategoryId));
        assertIsTrue(costsCount.get(costCategoryId) == 0, COST_CATEGORY_WITH_COSTS_MESSAGE_CODE);
    }
}
