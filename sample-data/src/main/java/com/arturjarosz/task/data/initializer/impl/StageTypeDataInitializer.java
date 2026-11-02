package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.dto.StageTypeDto;
import com.arturjarosz.task.project.application.StageTypeApplicationService;
import com.arturjarosz.task.project.infrastructure.repositor.StageTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Loads Stage types. Has to be run before projects are loaded, as stages of sample projects reference types by name.
 */
@Slf4j
@Component
@Order(9)
@RequiredArgsConstructor
public class StageTypeDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/stageTypesSample.json";
    private final StageTypeApplicationService stageTypeApplicationService;
    private final StageTypeRepository stageTypeRepository;

    @Override
    public void initializeData() {
        LOG.info("Starting importing stage types.");
        // types already loaded (e.g. data loaded more than once) are skipped, as their names have to be unique
        this.prepareStageTypes()
                .stream()
                .filter(stageType -> !this.stageTypeRepository.existsByNameIgnoreCase(stageType.getName()))
                .forEach(this.stageTypeApplicationService::create);
        LOG.info("Stage types added to the database.");
    }

    private List<StageTypeDto> prepareStageTypes() {
        return SampleDataFileReader.readList(FILE_NAME, StageTypeDto.class);
    }
}
