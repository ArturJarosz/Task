package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.dto.ArchitectDto;
import com.arturjarosz.task.exception.SampleDataInitializingException;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
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
        var mapper = new ObjectMapper();
        BaseValidator.assertNotEmpty(FILE_NAME, "File name cannot be empty.");
        try (InputStream inputStream = ArchitectDataInitializer.class.getClassLoader().getResourceAsStream(
                FILE_NAME)) {
            return mapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new SampleDataInitializingException(
                    String.format("There was a problem with adding architects from %1$s file", FILE_NAME), e);
        }
    }
}
