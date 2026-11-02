package com.arturjarosz.task.contractor.application.impl;

import com.arturjarosz.task.contractor.application.ContractorCategoryApplicationService;
import com.arturjarosz.task.contractor.application.ContractorCategoryValidator;
import com.arturjarosz.task.contractor.application.mapper.ContractorCategoryMapper;
import com.arturjarosz.task.contractor.infrastructure.ContractorCategoryRepository;
import com.arturjarosz.task.contractor.model.ContractorCategory;
import com.arturjarosz.task.dto.ContractorCategoryDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntryApplicationService;

@ApplicationService
public class ContractorCategoryApplicationServiceImpl extends AbstractNamedEntryApplicationService<ContractorCategory, ContractorCategoryDto>
        implements ContractorCategoryApplicationService {

    private final ContractorCategoryMapper contractorCategoryMapper;

    public ContractorCategoryApplicationServiceImpl(ContractorCategoryRepository contractorCategoryRepository, ContractorCategoryValidator contractorCategoryValidator, ContractorCategoryMapper contractorCategoryMapper) {
        super(contractorCategoryRepository, contractorCategoryValidator, "Contractor category");
        this.contractorCategoryMapper = contractorCategoryMapper;
    }

    @Override
    protected ContractorCategory createEntry(String name) {
        return new ContractorCategory(name);
    }

    @Override
    protected String nameOf(ContractorCategoryDto contractorCategoryDto) {
        return contractorCategoryDto.getName();
    }

    @Override
    protected ContractorCategoryDto toDto(ContractorCategory contractorCategory, Long numberOfUsages) {
        return this.contractorCategoryMapper.mapToDto(contractorCategory, numberOfUsages);
    }
}
