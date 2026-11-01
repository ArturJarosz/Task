package com.arturjarosz.task.configuration.application.entry.impl;

import com.arturjarosz.task.configuration.application.entry.ConfigurationProvider;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.dto.ConfigurationEntryDto;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Project types are configured by users, so the id of an entry is the id of the ProjectType and the label is its name.
 */
@Component
@RequiredArgsConstructor
public class ProjectTypeConfigurationProvider implements ConfigurationProvider {

    private final ProjectTypeRepository projectTypeRepository;

    @Override
    public ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        configurationDto.setProjectTypes(this.projectTypeRepository.findAll(Sort.by("name"))
                .stream()
                .map(projectType -> new ConfigurationEntryDto()
                        .id(String.valueOf(projectType.getId()))
                        .label(projectType.getName())
                ).toList());
        return configurationDto;
    }
}
