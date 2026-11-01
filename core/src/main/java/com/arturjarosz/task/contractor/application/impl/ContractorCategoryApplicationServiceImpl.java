package com.arturjarosz.task.contractor.application.impl;

import com.arturjarosz.task.dto.ContractorCategoryDto;
import com.arturjarosz.task.contractor.application.ContractorCategoryApplicationService;
import com.arturjarosz.task.contractor.application.ContractorCategoryValidator;
import com.arturjarosz.task.contractor.application.mapper.ContractorCategoryMapper;
import com.arturjarosz.task.contractor.infrastructure.ContractorCategoryRepository;
import com.arturjarosz.task.contractor.model.ContractorCategory;
import com.arturjarosz.task.contractor.query.ContractorQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ContractorCategoryApplicationServiceImpl implements ContractorCategoryApplicationService {

    private final ContractorCategoryRepository contractorCategoryRepository;
    private final ContractorQueryService contractorQueryService;
    private final ContractorCategoryValidator contractorCategoryValidator;
    private final ContractorCategoryMapper contractorCategoryMapper;

    @Transactional
    @Override
    public ContractorCategoryDto createContractorCategory(ContractorCategoryDto contractorCategoryDto) {
        LOG.info("Creating Contractor category.");

        this.contractorCategoryValidator.validateCreateContractorCategoryDto(contractorCategoryDto);
        var contractorCategory = this.saveWithUniqueName(new ContractorCategory(contractorCategoryDto.getName().strip()));

        LOG.info("Contractor category with id {} created.", contractorCategory.getId());
        return this.contractorCategoryMapper.mapToDto(contractorCategory, 0L);
    }

    @Transactional
    @Override
    public ContractorCategoryDto updateContractorCategory(Long contractorCategoryId, ContractorCategoryDto contractorCategoryDto) {
        LOG.info("Updating Contractor category with id {}.", contractorCategoryId);

        var maybeContractorCategory = this.contractorCategoryRepository.findById(contractorCategoryId);
        this.contractorCategoryValidator.validateContractorCategoryExistence(maybeContractorCategory, contractorCategoryId);
        this.contractorCategoryValidator.validateUpdateContractorCategoryDto(contractorCategoryDto, contractorCategoryId);
        var contractorCategory = maybeContractorCategory.orElseThrow(ResourceNotFoundException::new);
        contractorCategory.rename(contractorCategoryDto.getName().strip());
        contractorCategory = this.saveWithUniqueName(contractorCategory);

        LOG.info("Contractor category with id {} updated.", contractorCategoryId);
        return this.contractorCategoryMapper.mapToDto(contractorCategory, this.getNumberOfContractors(contractorCategoryId));
    }

    @Transactional
    @Override
    public void deleteContractorCategory(Long contractorCategoryId) {
        LOG.info("Deleting Contractor category with id {}.", contractorCategoryId);

        this.contractorCategoryValidator.validateContractorCategoryExistence(contractorCategoryId);
        this.contractorCategoryValidator.validateContractorCategoryHasNoContractors(contractorCategoryId);
        try {
            this.contractorCategoryRepository.deleteById(contractorCategoryId);
            // flushed here, so a contractor given this category after the check above fails the delete with the same
            // message
            this.contractorCategoryRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(ContractorCategoryValidator.CONTRACTOR_CATEGORY_WITH_CONTRACTORS_MESSAGE_CODE,
                    exception);
        }

        LOG.info("Contractor category with id {} deleted.", contractorCategoryId);
    }

    @Override
    public ContractorCategoryDto getContractorCategory(Long contractorCategoryId) {
        LOG.info("Loading Contractor category with id {}.", contractorCategoryId);

        var maybeContractorCategory = this.contractorCategoryRepository.findById(contractorCategoryId);
        this.contractorCategoryValidator.validateContractorCategoryExistence(maybeContractorCategory, contractorCategoryId);
        var contractorCategory = maybeContractorCategory.orElseThrow(ResourceNotFoundException::new);
        return this.contractorCategoryMapper.mapToDto(contractorCategory, this.getNumberOfContractors(contractorCategoryId));
    }

    @Override
    public List<ContractorCategoryDto> getContractorCategories() {
        LOG.info("Loading Contractor categories list.");

        var contractorCategories = this.contractorCategoryRepository.findAll(Sort.by("name"));
        var contractorsCountByCategoryId = this.contractorQueryService.getNumberOfContractorsByCategoryIds(
                contractorCategories.stream().map(ContractorCategory::getId).toList());
        return contractorCategories.stream()
                .map(contractorCategory -> this.contractorCategoryMapper.mapToDto(contractorCategory,
                        contractorsCountByCategoryId.get(contractorCategory.getId())))
                .toList();
    }

    /**
     * Saves and flushes, so a category given the same name in the meantime (after the validator checked it) fails
     * with the same message as the validator.
     */
    private ContractorCategory saveWithUniqueName(ContractorCategory contractorCategory) {
        try {
            return this.contractorCategoryRepository.saveAndFlush(contractorCategory);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(ContractorCategoryValidator.CONTRACTOR_CATEGORY_NAME_UNIQUE_MESSAGE_CODE,
                    exception, contractorCategory.getName());
        }
    }

    private Long getNumberOfContractors(Long contractorCategoryId) {
        return this.contractorQueryService.getNumberOfContractorsByCategoryIds(List.of(contractorCategoryId)).get(contractorCategoryId);
    }
}
