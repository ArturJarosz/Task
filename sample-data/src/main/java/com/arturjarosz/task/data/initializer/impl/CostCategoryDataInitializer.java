package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.dto.CostCategoryDto;
import com.arturjarosz.task.finance.application.CostCategoryApplicationService;
import com.arturjarosz.task.finance.infrastructure.CostCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Loads Cost categories. Has to be run before projects are loaded, as costs of sample projects reference categories by
 * name.
 */
@Slf4j
@Component
@Order(8)
@RequiredArgsConstructor
public class CostCategoryDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/costCategoriesSample.json";
    private final CostCategoryApplicationService costCategoryApplicationService;
    private final CostCategoryRepository costCategoryRepository;

    @Override
    public void initializeData() {
        LOG.info("Starting importing cost categories.");
        // categories already loaded (e.g. data loaded more than once) are skipped, as their names have to be unique
        this.prepareCostCategories()
                .stream()
                .filter(category -> !this.costCategoryRepository.existsByNameIgnoreCase(category.getName()))
                .forEach(this.costCategoryApplicationService::createCostCategory);
        LOG.info("Cost categories added to the database.");
    }

    private List<CostCategoryDto> prepareCostCategories() {
        return SampleDataFileReader.readList(FILE_NAME, CostCategoryDto.class);
    }
}
