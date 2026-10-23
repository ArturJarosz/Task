package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.dto.ArchitectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(1)
public class ArchitectDataInitializer implements DataInitializer {
    private final static String FILE_NAME = "sampleData/architectsSample.json";
    private final ArchitectApplicationService architectApplicationService;

    @Autowired
    public ArchitectDataInitializer(ArchitectApplicationService architectApplicationService) {
        this.architectApplicationService = architectApplicationService;
    }

    @Override
    public void initializeData() {
        LOG.info("Starting importing architects.");
        this.importArchitectsFromFile();
        LOG.info("Architects added to the database.");
    }

    private void importArchitectsFromFile() {
        List<ArchitectDto> architectDtos = this.prepareArchitects();
        architectDtos.forEach(this.architectApplicationService::createArchitect);
    }

    private List<ArchitectDto> prepareArchitects() {
        return SampleDataFileReader.readList(FILE_NAME, ArchitectDto.class);
    }
}
