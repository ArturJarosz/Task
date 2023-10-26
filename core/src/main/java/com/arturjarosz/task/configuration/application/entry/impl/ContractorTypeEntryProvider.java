package com.arturjarosz.task.configuration.application.entry.impl;

import com.arturjarosz.task.configuration.application.entry.EntryProvider;
import com.arturjarosz.task.contractor.model.ContractorCategory;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.dto.ConfigurationEntryDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ContractorTypeEntryProvider implements EntryProvider {

    @Override
    public ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        configurationDto.setContractorTypes(Arrays.stream(ContractorCategory.values())
                .map(category -> new ConfigurationEntryDto()
                        .id(category.name())
                        .label(this.createLabel(category.name()))
                ).toList());
        return configurationDto;
    }
}
