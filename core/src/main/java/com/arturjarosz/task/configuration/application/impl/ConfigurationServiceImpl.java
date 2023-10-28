package com.arturjarosz.task.configuration.application.impl;

import com.arturjarosz.task.configuration.application.ConfigurationService;
import com.arturjarosz.task.configuration.application.entry.ConfigurationProvider;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConfigurationServiceImpl implements ConfigurationService {

    @NonNull
    private final List<ConfigurationProvider> configurationProviders;

    @Override
    public ApplicationConfigurationDto getApplicationConfiguration() {
        var configuration = new ApplicationConfigurationDto();
        this.configurationProviders.forEach(provider -> provider.addConfigurationEntry(configuration));
        return configuration;
    }
}
