package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.contractor.application.ContractorCategoryApplicationService;
import com.arturjarosz.task.contractor.infrastructure.ContractorCategoryRepository;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.dto.ContractorCategoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Loads Contractor categories. Has to be run before contractors are loaded, as sample contractors reference categories by name.
 */
@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class ContractorCategoryDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/contractorCategoriesSample.json";
    private final ContractorCategoryApplicationService contractorCategoryApplicationService;
    private final ContractorCategoryRepository contractorCategoryRepository;

    @Override
    public void initializeData() {
        LOG.info("Starting importing contractor categories.");
        // categories already loaded (e.g. data loaded more than once) are skipped, as their names have to be unique
        this.prepareContractorCategories()
                .stream()
                .filter(category -> !this.contractorCategoryRepository.existsByNameIgnoreCase(category.getName()))
                .forEach(this.contractorCategoryApplicationService::createContractorCategory);
        LOG.info("Contractor categories added to the database.");
    }

    private List<ContractorCategoryDto> prepareContractorCategories() {
        return SampleDataFileReader.readList(FILE_NAME, ContractorCategoryDto.class);
    }
}
