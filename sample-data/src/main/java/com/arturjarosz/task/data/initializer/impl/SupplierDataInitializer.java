package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.supplier.application.SupplierApplicationService;
import com.arturjarosz.task.supplier.infrastructure.SupplierCategoryRepository;
import com.arturjarosz.task.supplier.model.SupplierCategory;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.data.model.PartnerSample;
import com.arturjarosz.task.dto.SupplierDto;
import com.arturjarosz.task.exception.SampleDataInitializingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads suppliers. Sample suppliers reference their category by name, so categories have to be loaded first.
 */
@Slf4j
@Component
@Order(6)
@RequiredArgsConstructor
public class SupplierDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/supplierSample.json";
    private final SupplierApplicationService supplierApplicationService;
    private final SupplierCategoryRepository supplierCategoryRepository;

    @Override
    public void initializeData() {
        LOG.info("Starting importing suppliers.");
        // When a name is not unique (e.g. data loaded more than once), the category with the lowest id is used
        Map<String, Long> categoryIdsByName = this.supplierCategoryRepository.findAll()
                .stream()
                .collect(Collectors.toMap(SupplierCategory::getName, SupplierCategory::getId, Math::min));
        this.prepareSuppliers().forEach(sample -> this.supplierApplicationService.createSupplier(new SupplierDto()
                .name(sample.getName())
                .categoryId(getCategoryId(categoryIdsByName, sample.getCategory()))
                .email(sample.getEmail())
                .telephone(sample.getTelephone())
                .note(sample.getNote())));
        LOG.info("Suppliers added to the database.");
    }

    private static Long getCategoryId(Map<String, Long> categoryIdsByName, String categoryName) {
        var categoryId = categoryIdsByName.get(categoryName);
        if (categoryId == null) {
            throw new SampleDataInitializingException(
                    String.format("Supplier category %1$s does not exist", categoryName), null);
        }
        return categoryId;
    }

    private List<PartnerSample> prepareSuppliers() {
        return SampleDataFileReader.readList(FILE_NAME, PartnerSample.class);
    }
}
