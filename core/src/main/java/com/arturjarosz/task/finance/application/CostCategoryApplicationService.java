package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.dto.CostCategoryDto;

import java.util.List;

public interface CostCategoryApplicationService {

    /**
     * Create new CostCategory with name from CostCategoryDto. If the name is missing, too long or already used by
     * another CostCategory, an exception is thrown.
     */
    CostCategoryDto createCostCategory(CostCategoryDto costCategoryDto);

    /**
     * Rename CostCategory of given costCategoryId. If CostCategory does not exist or the name is not correct, an
     * exception is thrown.
     */
    CostCategoryDto updateCostCategory(Long costCategoryId, CostCategoryDto costCategoryDto);

    /**
     * Remove CostCategory with given costCategoryId. If CostCategory does not exist or any Cost has that
     * category, an exception is thrown.
     */
    void deleteCostCategory(Long costCategoryId);

    /**
     * Get CostCategory of given costCategoryId with the number of its Costs. If CostCategory does not exist,
     * an exception is thrown.
     */
    CostCategoryDto getCostCategory(Long costCategoryId);

    /**
     * Get all CostCategories sorted by name, each with the number of its Costs.
     */
    List<CostCategoryDto> getCostCategories();
}
