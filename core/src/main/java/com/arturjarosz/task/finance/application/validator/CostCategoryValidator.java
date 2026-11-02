package com.arturjarosz.task.finance.application.validator;

import com.arturjarosz.task.finance.infrastructure.CostCategoryRepository;
import com.arturjarosz.task.finance.model.CostCategory;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryValidator;
import org.springframework.stereotype.Component;

/**
 * Validates cost category dtos and CostCategory entities.
 */
@Component
public class CostCategoryValidator extends NamedEntryValidator<CostCategory> {

    public CostCategoryValidator(CostCategoryRepository costCategoryRepository, FinancialDataQueryService financialDataQueryService) {
        super(costCategoryRepository, ProjectExceptionCodes.COST_CATEGORY, ProjectExceptionCodes.COSTS, financialDataQueryService::getNumberOfCostsByCategoryIds);
    }
}
