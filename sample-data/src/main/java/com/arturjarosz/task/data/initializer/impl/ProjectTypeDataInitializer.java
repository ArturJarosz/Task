package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.dto.ProjectTypeDto;
import com.arturjarosz.task.project.application.ProjectTypeApplicationService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Loads Project types. Has to be run before projects are loaded, as sample projects reference types by name.
 */
@Slf4j
@Component
@Order(7)
@RequiredArgsConstructor
public class ProjectTypeDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/projectTypesSample.json";
    private final ProjectTypeApplicationService projectTypeApplicationService;
    private final ProjectTypeRepository projectTypeRepository;

    @Override
    public void initializeData() {
        LOG.info("Starting importing project types.");
        // types already loaded (e.g. data loaded more than once) are skipped, as their names have to be unique
        this.prepareProjectTypes()
                .stream()
                .filter(projectType -> !this.projectTypeRepository.existsByNameIgnoreCase(projectType.getName()))
                .forEach(this.projectTypeApplicationService::createProjectType);
        LOG.info("Project types added to the database.");
    }

    private List<ProjectTypeDto> prepareProjectTypes() {
        return SampleDataFileReader.readList(FILE_NAME, ProjectTypeDto.class);
    }
}
