package com.arturjarosz.task.companycost.application;

import com.arturjarosz.task.dto.CompanyCostCategoryDto;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryApplicationService;

/**
 * Company cost categories configured by users, each with the number of company costs using it.
 */
public interface CompanyCostCategoryApplicationService extends NamedEntryApplicationService<CompanyCostCategoryDto> {
}
