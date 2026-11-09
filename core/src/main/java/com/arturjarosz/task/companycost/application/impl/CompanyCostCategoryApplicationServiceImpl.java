package com.arturjarosz.task.companycost.application.impl;

import com.arturjarosz.task.companycost.application.CompanyCostCategoryApplicationService;
import com.arturjarosz.task.companycost.application.CompanyCostCategoryValidator;
import com.arturjarosz.task.companycost.application.mapper.CompanyCostCategoryMapper;
import com.arturjarosz.task.companycost.infrastructure.CompanyCostCategoryRepository;
import com.arturjarosz.task.companycost.model.CompanyCostCategory;
import com.arturjarosz.task.dto.CompanyCostCategoryDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntryApplicationService;

@ApplicationService
public class CompanyCostCategoryApplicationServiceImpl
        extends AbstractNamedEntryApplicationService<CompanyCostCategory, CompanyCostCategoryDto>
        implements CompanyCostCategoryApplicationService {

    private final CompanyCostCategoryMapper companyCostCategoryMapper;

    public CompanyCostCategoryApplicationServiceImpl(CompanyCostCategoryRepository companyCostCategoryRepository,
            CompanyCostCategoryValidator companyCostCategoryValidator,
            CompanyCostCategoryMapper companyCostCategoryMapper) {
        super(companyCostCategoryRepository, companyCostCategoryValidator, "Company cost category");
        this.companyCostCategoryMapper = companyCostCategoryMapper;
    }

    @Override
    protected CompanyCostCategory createEntry(String name) {
        return new CompanyCostCategory(name);
    }

    @Override
    protected String nameOf(CompanyCostCategoryDto companyCostCategoryDto) {
        return companyCostCategoryDto.getName();
    }

    @Override
    protected CompanyCostCategoryDto toDto(CompanyCostCategory companyCostCategory, Long numberOfUsages) {
        return this.companyCostCategoryMapper.mapToDto(companyCostCategory, numberOfUsages);
    }
}
