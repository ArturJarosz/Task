package com.arturjarosz.task.configuration.application.entry.impl;

import com.arturjarosz.task.configuration.application.entry.ConfigurationProvider;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.dto.ConfigurationEntryDto;
import com.arturjarosz.task.supplier.model.SupplierCategory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SuppliersTypeConfigurationProvider implements ConfigurationProvider {

    @Override
    public ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        configurationDto.setSupplierTypes(Arrays.stream(SupplierCategory.values())
                .map(category -> new ConfigurationEntryDto()
                        .id(category.name())
                        .label(this.createLabel(category.name()))
                ).toList());
        return configurationDto;
    }
}
