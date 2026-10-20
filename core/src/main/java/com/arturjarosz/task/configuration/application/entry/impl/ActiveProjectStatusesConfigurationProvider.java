package com.arturjarosz.task.configuration.application.entry.impl;

import com.arturjarosz.task.configuration.application.entry.ConfigurationProvider;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.dto.ProjectStatusDto;
import com.arturjarosz.task.properties.ProjectProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActiveProjectStatusesConfigurationProvider implements ConfigurationProvider {

    @NonNull
    private final ProjectProperties projectProperties;

    @Override
    public ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        configurationDto.setActiveProjectStatuses(this.projectProperties.activeStatuses()
                .stream()
                .map(status -> ProjectStatusDto.valueOf(status.name()))
                .sorted()
                .toList());
        return configurationDto;
    }
}
