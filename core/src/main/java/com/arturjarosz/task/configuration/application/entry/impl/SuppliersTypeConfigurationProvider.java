package com.arturjarosz.task.configuration.application.entry.impl;

import com.arturjarosz.task.configuration.application.entry.ConfigurationProvider;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.dto.ConfigurationEntryDto;
import com.arturjarosz.task.supplier.infrastructure.SupplierCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Supplier categories are configured by users, so the id of an entry is the id of the SupplierCategory and the label is
 * its name.
 */
@Component
@RequiredArgsConstructor
public class SuppliersTypeConfigurationProvider implements ConfigurationProvider {

    private final SupplierCategoryRepository supplierCategoryRepository;

    @Override
    public ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        configurationDto.setSupplierTypes(this.supplierCategoryRepository.findAll(Sort.by("name"))
                .stream()
                .map(category -> new ConfigurationEntryDto()
                        .id(String.valueOf(category.getId()))
                        .label(category.getName())
                ).toList());
        return configurationDto;
    }
}
