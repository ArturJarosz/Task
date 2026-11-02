package com.arturjarosz.task.configuration.application.entry.impl;

import com.arturjarosz.task.configuration.application.entry.ConfigurationProvider;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.dto.ConfigurationEntryDto;
import com.arturjarosz.task.project.infrastructure.repositor.StageTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Stage types are configured by users, so the id of an entry is the id of the StageType and the label is its name.
 */
@Component
@RequiredArgsConstructor
public class StageTypeConfigurationProvider implements ConfigurationProvider {

    private final StageTypeRepository stageTypeRepository;

    @Override
    public ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        configurationDto.setStageTypes(this.stageTypeRepository.findAll(Sort.by("name"))
                .stream()
                .map(stageType -> new ConfigurationEntryDto()
                        .id(String.valueOf(stageType.getId()))
                        .label(stageType.getName())
                ).toList());
        return configurationDto;
    }
}
