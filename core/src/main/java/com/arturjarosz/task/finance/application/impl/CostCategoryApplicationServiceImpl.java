package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.dto.CostCategoryDto;
import com.arturjarosz.task.finance.application.CostCategoryApplicationService;
import com.arturjarosz.task.finance.application.mapper.CostCategoryMapper;
import com.arturjarosz.task.finance.application.validator.CostCategoryValidator;
import com.arturjarosz.task.finance.infrastructure.CostCategoryRepository;
import com.arturjarosz.task.finance.model.CostCategory;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntryApplicationService;

@ApplicationService
public class CostCategoryApplicationServiceImpl extends AbstractNamedEntryApplicationService<CostCategory, CostCategoryDto>
        implements CostCategoryApplicationService {

    private final CostCategoryMapper costCategoryMapper;

    public CostCategoryApplicationServiceImpl(CostCategoryRepository costCategoryRepository, CostCategoryValidator costCategoryValidator, CostCategoryMapper costCategoryMapper) {
        super(costCategoryRepository, costCategoryValidator, "Cost category");
        this.costCategoryMapper = costCategoryMapper;
    }

    @Override
    protected CostCategory createEntry(String name) {
        return new CostCategory(name);
    }

    @Override
    protected String nameOf(CostCategoryDto costCategoryDto) {
        return costCategoryDto.getName();
    }

    @Override
    protected CostCategoryDto toDto(CostCategory costCategory, Long numberOfUsages) {
        return this.costCategoryMapper.mapToDto(costCategory, numberOfUsages);
    }
}
