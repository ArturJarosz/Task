package com.arturjarosz.task.configuration.application.entry.impl;

import com.arturjarosz.task.client.model.ClientType;
import com.arturjarosz.task.configuration.application.entry.ConfigurationProvider;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.dto.ConfigurationEntryDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ClientTypeConfigurationProvider implements ConfigurationProvider {

    @Override
    public ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        configurationDto.setClientTypes(Arrays.stream(ClientType.values())
                .map(type -> new ConfigurationEntryDto()
                        .id(type.name())
                        .label(this.createLabel(type.name()))
                ).toList());
        return configurationDto;
    }
}
