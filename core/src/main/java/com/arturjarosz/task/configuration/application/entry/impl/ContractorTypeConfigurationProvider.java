package com.arturjarosz.task.configuration.application.entry.impl;

import com.arturjarosz.task.configuration.application.entry.ConfigurationProvider;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.dto.ConfigurationEntryDto;
import com.arturjarosz.task.contractor.infrastructure.ContractorCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Contractor categories are configured by users, so the id of an entry is the id of the ContractorCategory and the label is
 * its name.
 */
@Component
@RequiredArgsConstructor
public class ContractorTypeConfigurationProvider implements ConfigurationProvider {

    private final ContractorCategoryRepository contractorCategoryRepository;

    @Override
    public ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        configurationDto.setContractorTypes(this.contractorCategoryRepository.findAll(Sort.by("name"))
                .stream()
                .map(category -> new ConfigurationEntryDto()
                        .id(String.valueOf(category.getId()))
                        .label(category.getName())
                ).toList());
        return configurationDto;
    }
}
