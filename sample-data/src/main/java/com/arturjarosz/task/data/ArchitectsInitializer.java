package com.arturjarosz.task.data;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.dto.ArchitectDto;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

@Component
public class ArchitectsInitializer implements DataInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ArchitectsInitializer.class);

    private final ArchitectApplicationService architectApplicationService;

    @Autowired
    public ArchitectsInitializer(ArchitectApplicationService architectApplicationService) {
        this.architectApplicationService = architectApplicationService;
    }

    @Override
    public void initializeData() {
        LOG.info("Starting importing architects.");
        this.importArchitectsFromFile();
        LOG.info("Architects added to the database.");
    }

    private void importArchitectsFromFile() {
        List<ArchitectDto> architectDtos = this.prepareArchitects("architectsSample.json");
        architectDtos.forEach(this.architectApplicationService::createArchitect);
    }

    private List<ArchitectDto> prepareArchitects(String filename) {
        var mapper = new ObjectMapper();
        BaseValidator.assertNotEmpty(filename, "File name cannot be empty.");
        try (InputStream inputStream = ArchitectsInitializer.class.getClassLoader().getResourceAsStream(filename)) {
            return mapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new UncheckedIOException(
                    String.format("There was a problem with adding architects from %1$s file", filename), e);
        }
    }
}
