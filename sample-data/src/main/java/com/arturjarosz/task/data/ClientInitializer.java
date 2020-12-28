package com.arturjarosz.task.data;

import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.client.application.dto.ClientBasicDto;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

@Component
public class ClientInitializer {

    private final Logger LOG = LogManager.getLogger(ClientInitializer.class);

    private final ClientApplicationService clientApplicationService;

    @Autowired
    public ClientInitializer(ClientApplicationService clientApplicationService) {
        this.clientApplicationService = clientApplicationService;
    }

    void run() {
        this.LOG.info("Starting importing clients.");
        this.importClientsFromFile();
        this.LOG.info("Clients added to the database.");
    }

    private void importClientsFromFile() {
        List<ClientBasicDto> clientBasicDtos = this.prepareClients("clientsSample.json");
        clientBasicDtos.forEach(this.clientApplicationService::createClient);
    }

    private List<ClientBasicDto> prepareClients(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        BaseValidator.assertNotEmpty(filename, "File name cannot be empty.");
        try (InputStream inputStream = ClientInitializer.class.getClassLoader().getResourceAsStream(filename)) {
            return mapper.readValue(inputStream, new TypeReference<List<ClientBasicDto>>() {
            });
        } catch (IOException e) {
            throw new UncheckedIOException(
                    String.format("There was a problem with adding clients from %1$s file", filename), e);
        }
    }
}
