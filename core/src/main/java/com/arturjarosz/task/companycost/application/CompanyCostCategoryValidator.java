package com.arturjarosz.task.companycost.application;

import com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes;
import com.arturjarosz.task.companycost.infrastructure.CompanyCostCategoryRepository;
import com.arturjarosz.task.companycost.model.CompanyCostCategory;
import com.arturjarosz.task.companycost.query.CompanyCostQueryService;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryValidator;
import org.springframework.stereotype.Component;

/**
 * Validates company cost category dtos and CompanyCostCategory entities.
 */
@Component
public class CompanyCostCategoryValidator extends NamedEntryValidator<CompanyCostCategory> {

    public CompanyCostCategoryValidator(CompanyCostCategoryRepository companyCostCategoryRepository,
            CompanyCostQueryService companyCostQueryService) {
        super(companyCostCategoryRepository, CompanyCostExceptionCodes.COMPANY_COST_CATEGORY,
                CompanyCostExceptionCodes.COMPANY_COSTS, companyCostQueryService::getNumberOfCompanyCostsByCategoryIds);
    }
}
