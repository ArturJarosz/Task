package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.dto.TaskTypeDto;
import com.arturjarosz.task.project.application.TaskTypeApplicationService;
import com.arturjarosz.task.project.infrastructure.repositor.TaskTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Loads Task types. Has to be run before projects are loaded, as tasks of sample projects reference types by name.
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class TaskTypeDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/taskTypesSample.json";
    private final TaskTypeApplicationService taskTypeApplicationService;
    private final TaskTypeRepository taskTypeRepository;

    @Override
    public void initializeData() {
        LOG.info("Starting importing task types.");
        // types already loaded (e.g. data loaded more than once) are skipped, as their names have to be unique
        this.prepareTaskTypes()
                .stream()
                .filter(taskType -> !this.taskTypeRepository.existsByNameIgnoreCase(taskType.getName()))
                .forEach(this.taskTypeApplicationService::create);
        LOG.info("Task types added to the database.");
    }

    private List<TaskTypeDto> prepareTaskTypes() {
        return SampleDataFileReader.readList(FILE_NAME, TaskTypeDto.class);
    }
}
