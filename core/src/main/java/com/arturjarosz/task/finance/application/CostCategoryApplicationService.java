package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.dto.CostCategoryDto;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryApplicationService;

/**
 * Cost categorys configured by users, each with the number of items using it.
 */
public interface CostCategoryApplicationService extends NamedEntryApplicationService<CostCategoryDto> {
}
