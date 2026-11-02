package com.arturjarosz.task.contractor.application;

import com.arturjarosz.task.dto.ContractorCategoryDto;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryApplicationService;

/**
 * Contractor categorys configured by users, each with the number of items using it.
 */
public interface ContractorCategoryApplicationService extends NamedEntryApplicationService<ContractorCategoryDto> {
}
