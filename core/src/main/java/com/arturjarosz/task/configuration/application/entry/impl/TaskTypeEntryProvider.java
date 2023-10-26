package com.arturjarosz.task.configuration.application.entry.impl;

import com.arturjarosz.task.configuration.application.entry.EntryProvider;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.dto.ConfigurationEntryDto;
import com.arturjarosz.task.project.model.TaskType;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class TaskTypeEntryProvider implements EntryProvider {

    @Override
    public ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        configurationDto.setTaskTypes(Arrays.stream(TaskType.values())
                .map(category -> new ConfigurationEntryDto()
                        .id(category.name())
                        .label(this.createLabel(category.name()))
                ).toList());
        return configurationDto;
    }
}
