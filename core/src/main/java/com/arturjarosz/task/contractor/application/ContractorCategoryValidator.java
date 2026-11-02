package com.arturjarosz.task.contractor.application;

import com.arturjarosz.task.contractor.domain.ContractorExceptionCodes;
import com.arturjarosz.task.contractor.infrastructure.ContractorCategoryRepository;
import com.arturjarosz.task.contractor.model.ContractorCategory;
import com.arturjarosz.task.contractor.query.ContractorQueryService;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryValidator;
import org.springframework.stereotype.Component;

/**
 * Validates contractor category dtos and ContractorCategory entities.
 */
@Component
public class ContractorCategoryValidator extends NamedEntryValidator<ContractorCategory> {

    public ContractorCategoryValidator(ContractorCategoryRepository contractorCategoryRepository, ContractorQueryService contractorQueryService) {
        super(contractorCategoryRepository, ContractorExceptionCodes.CONTRACTOR_CATEGORY, ContractorExceptionCodes.CONTRACTORS, contractorQueryService::getNumberOfContractorsByCategoryIds);
    }
}
