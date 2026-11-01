package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.contractor.application.ContractorApplicationService;
import com.arturjarosz.task.contractor.infrastructure.ContractorCategoryRepository;
import com.arturjarosz.task.contractor.model.ContractorCategory;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.data.model.PartnerSample;
import com.arturjarosz.task.dto.ContractorDto;
import com.arturjarosz.task.exception.SampleDataInitializingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads contractors. Sample contractors reference their category by name, so categories have to be loaded first.
 */
@Slf4j
@Component
@Order(4)
@RequiredArgsConstructor
public class ContractorDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/contractorsSample.json";
    private final ContractorApplicationService contractorApplicationService;
    private final ContractorCategoryRepository contractorCategoryRepository;

    @Override
    public void initializeData() {
        LOG.info("Starting importing contractors.");
        // When a name is not unique (e.g. data loaded more than once), the category with the lowest id is used
        Map<String, Long> categoryIdsByName = this.contractorCategoryRepository.findAll()
                .stream()
                .collect(Collectors.toMap(ContractorCategory::getName, ContractorCategory::getId, Math::min));
        this.prepareContractors().forEach(sample -> this.contractorApplicationService.createContractor(new ContractorDto()
                .name(sample.getName())
                .categoryId(getCategoryId(categoryIdsByName, sample.getCategory()))
                .email(sample.getEmail())
                .telephone(sample.getTelephone())
                .note(sample.getNote())));
        LOG.info("Contractors added to the database.");
    }

    private static Long getCategoryId(Map<String, Long> categoryIdsByName, String categoryName) {
        var categoryId = categoryIdsByName.get(categoryName);
        if (categoryId == null) {
            throw new SampleDataInitializingException(
                    String.format("Contractor category %1$s does not exist", categoryName), null);
        }
        return categoryId;
    }

    private List<PartnerSample> prepareContractors() {
        return SampleDataFileReader.readList(FILE_NAME, PartnerSample.class);
    }
}
