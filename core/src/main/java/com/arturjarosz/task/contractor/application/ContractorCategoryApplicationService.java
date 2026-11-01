package com.arturjarosz.task.contractor.application;

import com.arturjarosz.task.dto.ContractorCategoryDto;

import java.util.List;

public interface ContractorCategoryApplicationService {

    /**
     * Create new ContractorCategory with name from ContractorCategoryDto. If the name is missing, too long or already used by
     * another ContractorCategory, an exception is thrown.
     */
    ContractorCategoryDto createContractorCategory(ContractorCategoryDto contractorCategoryDto);

    /**
     * Rename ContractorCategory of given contractorCategoryId. If ContractorCategory does not exist or the name is not correct, an
     * exception is thrown.
     */
    ContractorCategoryDto updateContractorCategory(Long contractorCategoryId, ContractorCategoryDto contractorCategoryDto);

    /**
     * Remove ContractorCategory with given contractorCategoryId. If ContractorCategory does not exist or any Contractor has that
     * category, an exception is thrown.
     */
    void deleteContractorCategory(Long contractorCategoryId);

    /**
     * Get ContractorCategory of given contractorCategoryId with the number of its Contractors. If ContractorCategory does not exist,
     * an exception is thrown.
     */
    ContractorCategoryDto getContractorCategory(Long contractorCategoryId);

    /**
     * Get all ContractorCategories sorted by name, each with the number of its Contractors.
     */
    List<ContractorCategoryDto> getContractorCategories();
}
