package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.dto.CostCategoryDto;
import com.arturjarosz.task.finance.application.CostCategoryApplicationService;
import com.arturjarosz.task.finance.application.validator.CostCategoryValidator;
import com.arturjarosz.task.finance.application.mapper.CostCategoryMapper;
import com.arturjarosz.task.finance.infrastructure.CostCategoryRepository;
import com.arturjarosz.task.finance.model.CostCategory;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
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
public class CostCategoryApplicationServiceImpl implements CostCategoryApplicationService {

    private final CostCategoryRepository costCategoryRepository;
    private final FinancialDataQueryService financialDataQueryService;
    private final CostCategoryValidator costCategoryValidator;
    private final CostCategoryMapper costCategoryMapper;

    @Transactional
    @Override
    public CostCategoryDto createCostCategory(CostCategoryDto costCategoryDto) {
        LOG.info("Creating Cost category.");

        this.costCategoryValidator.validateCreateCostCategoryDto(costCategoryDto);
        var costCategory = this.saveWithUniqueName(new CostCategory(costCategoryDto.getName().strip()));

        LOG.info("Cost category with id {} created.", costCategory.getId());
        return this.costCategoryMapper.mapToDto(costCategory, 0L);
    }

    @Transactional
    @Override
    public CostCategoryDto updateCostCategory(Long costCategoryId, CostCategoryDto costCategoryDto) {
        LOG.info("Updating Cost category with id {}.", costCategoryId);

        var maybeCostCategory = this.costCategoryRepository.findById(costCategoryId);
        this.costCategoryValidator.validateCostCategoryExistence(maybeCostCategory, costCategoryId);
        this.costCategoryValidator.validateUpdateCostCategoryDto(costCategoryDto, costCategoryId);
        var costCategory = maybeCostCategory.orElseThrow(ResourceNotFoundException::new);
        costCategory.rename(costCategoryDto.getName().strip());
        costCategory = this.saveWithUniqueName(costCategory);

        LOG.info("Cost category with id {} updated.", costCategoryId);
        return this.costCategoryMapper.mapToDto(costCategory, this.getNumberOfCosts(costCategoryId));
    }

    @Transactional
    @Override
    public void deleteCostCategory(Long costCategoryId) {
        LOG.info("Deleting Cost category with id {}.", costCategoryId);

        this.costCategoryValidator.validateCostCategoryExistence(costCategoryId);
        this.costCategoryValidator.validateCostCategoryHasNoCosts(costCategoryId);
        try {
            this.costCategoryRepository.deleteById(costCategoryId);
            // flushed here, so a cost given this category after the check above fails the delete with the same
            // message
            this.costCategoryRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(CostCategoryValidator.COST_CATEGORY_WITH_COSTS_MESSAGE_CODE,
                    exception);
        }

        LOG.info("Cost category with id {} deleted.", costCategoryId);
    }

    @Override
    public CostCategoryDto getCostCategory(Long costCategoryId) {
        LOG.info("Loading Cost category with id {}.", costCategoryId);

        var maybeCostCategory = this.costCategoryRepository.findById(costCategoryId);
        this.costCategoryValidator.validateCostCategoryExistence(maybeCostCategory, costCategoryId);
        var costCategory = maybeCostCategory.orElseThrow(ResourceNotFoundException::new);
        return this.costCategoryMapper.mapToDto(costCategory, this.getNumberOfCosts(costCategoryId));
    }

    @Override
    public List<CostCategoryDto> getCostCategories() {
        LOG.info("Loading Cost categories list.");

        var costCategories = this.costCategoryRepository.findAll(Sort.by("name"));
        var costsCountByCategoryId = this.financialDataQueryService.getNumberOfCostsByCategoryIds(
                costCategories.stream().map(CostCategory::getId).toList());
        return costCategories.stream()
                .map(costCategory -> this.costCategoryMapper.mapToDto(costCategory,
                        costsCountByCategoryId.get(costCategory.getId())))
                .toList();
    }

    /**
     * Saves and flushes, so a category given the same name in the meantime (after the validator checked it) fails
     * with the same message as the validator.
     */
    private CostCategory saveWithUniqueName(CostCategory costCategory) {
        try {
            return this.costCategoryRepository.saveAndFlush(costCategory);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(CostCategoryValidator.COST_CATEGORY_NAME_UNIQUE_MESSAGE_CODE,
                    exception, costCategory.getName());
        }
    }

    private Long getNumberOfCosts(Long costCategoryId) {
        return this.financialDataQueryService.getNumberOfCostsByCategoryIds(List.of(costCategoryId)).get(costCategoryId);
    }
}
