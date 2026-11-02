package com.arturjarosz.task.configuration.application.entry.impl;

import com.arturjarosz.task.configuration.application.entry.ConfigurationProvider;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.dto.ConfigurationEntryDto;
import com.arturjarosz.task.project.infrastructure.repositor.TaskTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Task types are configured by users, so the id of an entry is the id of the TaskType and the label is its name.
 */
@Component
@RequiredArgsConstructor
public class TaskTypeConfigurationProvider implements ConfigurationProvider {

    private final TaskTypeRepository taskTypeRepository;

    @Override
    public ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        configurationDto.setTaskTypes(this.taskTypeRepository.findAll(Sort.by("name"))
                .stream()
                .map(taskType -> new ConfigurationEntryDto()
                        .id(String.valueOf(taskType.getId()))
                        .label(taskType.getName())
                ).toList());
        return configurationDto;
    }
}
