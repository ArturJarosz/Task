package com.arturjarosz.task.data;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

@Component
public class ArchitectsInitializer {

    private static final Logger LOG = LogManager.getLogger(ArchitectsInitializer.class);

    private final ArchitectApplicationService architectApplicationService;

    @Autowired
    public ArchitectsInitializer(ArchitectApplicationService architectApplicationService) {
        this.architectApplicationService = architectApplicationService;
    }

    @Transactional
    void run() {
        LOG.info("Starting importing architects.");
        this.importArchitectsFromFile();
        LOG.info("Architects added to the database.");
    }

    private void importArchitectsFromFile() {
        List<ArchitectBasicDto> architectBasicDtos = this.prepareArchitects("architectsSample.json");
        architectBasicDtos.forEach(this.architectApplicationService::createArchitect);
    }

    private List<ArchitectBasicDto> prepareArchitects(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        BaseValidator.assertNotEmpty(filename, "File name cannot be empty.");
        try (InputStream inputStream = ArchitectsInitializer.class.getClassLoader().getResourceAsStream(filename)) {
            return mapper.readValue(inputStream, new TypeReference<List<ArchitectBasicDto>>() {
            });
        } catch (IOException e) {
            throw new UncheckedIOException(
                    String.format("There was a problem with adding architects from %1$s file", filename), e);
        }
    }
}
