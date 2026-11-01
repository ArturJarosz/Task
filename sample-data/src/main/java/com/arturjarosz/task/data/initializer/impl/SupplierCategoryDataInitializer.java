package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.supplier.application.SupplierCategoryApplicationService;
import com.arturjarosz.task.supplier.infrastructure.SupplierCategoryRepository;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.dto.SupplierCategoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Loads Supplier categories. Has to be run before suppliers are loaded, as sample suppliers reference categories by name.
 */
@Slf4j
@Component
@Order(5)
@RequiredArgsConstructor
public class SupplierCategoryDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/supplierCategoriesSample.json";
    private final SupplierCategoryApplicationService supplierCategoryApplicationService;
    private final SupplierCategoryRepository supplierCategoryRepository;

    @Override
    public void initializeData() {
        LOG.info("Starting importing supplier categories.");
        // categories already loaded (e.g. data loaded more than once) are skipped, as their names have to be unique
        this.prepareSupplierCategories()
                .stream()
                .filter(category -> !this.supplierCategoryRepository.existsByNameIgnoreCase(category.getName()))
                .forEach(this.supplierCategoryApplicationService::createSupplierCategory);
        LOG.info("Supplier categories added to the database.");
    }

    private List<SupplierCategoryDto> prepareSupplierCategories() {
        return SampleDataFileReader.readList(FILE_NAME, SupplierCategoryDto.class);
    }
}
