package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.companycost.application.CompanyCostCategoryApplicationService;
import com.arturjarosz.task.companycost.infrastructure.CompanyCostCategoryRepository;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.dto.CompanyCostCategoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Loads Company cost categories. Has to be run before company costs are loaded, as they reference categories by name.
 */
@Slf4j
@Component
@Order(12)
@RequiredArgsConstructor
public class CompanyCostCategoryDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/companyCostCategoriesSample.json";
    private final CompanyCostCategoryApplicationService companyCostCategoryApplicationService;
    private final CompanyCostCategoryRepository companyCostCategoryRepository;

    @Override
    public void initializeData() {
        LOG.info("Starting importing company cost categories.");
        // categories already loaded (e.g. data loaded more than once) are skipped, as their names have to be unique
        this.prepareCompanyCostCategories()
                .stream()
                .filter(category -> !this.companyCostCategoryRepository.existsByNameIgnoreCase(category.getName()))
                .forEach(this.companyCostCategoryApplicationService::create);
        LOG.info("Company cost categories added to the database.");
    }

    private List<CompanyCostCategoryDto> prepareCompanyCostCategories() {
        return SampleDataFileReader.readList(FILE_NAME, CompanyCostCategoryDto.class);
    }
}
