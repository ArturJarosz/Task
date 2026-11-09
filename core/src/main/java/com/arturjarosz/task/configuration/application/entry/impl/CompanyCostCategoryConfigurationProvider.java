package com.arturjarosz.task.configuration.application.entry.impl;

import com.arturjarosz.task.companycost.infrastructure.CompanyCostCategoryRepository;
import com.arturjarosz.task.configuration.application.entry.ConfigurationProvider;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.dto.ConfigurationEntryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Company cost categories are configured by users, so the id of an entry is the id of the CompanyCostCategory and the
 * label is its name.
 */
@Component
@RequiredArgsConstructor
public class CompanyCostCategoryConfigurationProvider implements ConfigurationProvider {

    private final CompanyCostCategoryRepository companyCostCategoryRepository;

    @Override
    public ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        configurationDto.setCompanyCostCategories(this.companyCostCategoryRepository.findAll(Sort.by("name"))
                .stream()
                .map(category -> new ConfigurationEntryDto()
                        .id(String.valueOf(category.getId()))
                        .label(category.getName())
                ).toList());
        return configurationDto;
    }

}
