package com.arturjarosz.task.configuration.application.entry;

import com.arturjarosz.task.dto.ApplicationConfigurationDto;

public interface ConfigurationProvider {
    ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto);

    default String createLabel(String name) {
        return name.substring(0, 1).toUpperCase() +
                name.substring(1).toLowerCase().replace("_", " ");
    }
}
