package com.arturjarosz.task.configuration.application.entry.impl;

import com.arturjarosz.task.configuration.application.entry.EntryProvider;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.dto.ConfigurationEntryDto;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ProjectStatusEntryProvider implements EntryProvider {

    @Override
    public ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        configurationDto.setProjectStatuses(Arrays.stream(ProjectStatus.values())
                .map(category -> new ConfigurationEntryDto()
                        .id(category.name())
                        .label(this.createLabel(category.name()))
                ).toList());
        return configurationDto;
    }
}
