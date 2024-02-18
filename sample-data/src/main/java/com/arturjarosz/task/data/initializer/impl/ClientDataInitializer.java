package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.dto.ClientDto;
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
public class ClientDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/clientsSample.json";
    private final ClientApplicationService clientApplicationService;

    @Autowired
    public ClientDataInitializer(ClientApplicationService clientApplicationService) {
        this.clientApplicationService = clientApplicationService;
    }

    @Override
    public void initializeData() {
        LOG.info("Starting importing clients.");
        this.importClientsFromFile();
        LOG.info("Clients added to the database.");
    }

    private void importClientsFromFile() {
        List<ClientDto> clientDtos = this.prepareClients();
        clientDtos.forEach(this.clientApplicationService::createClient);
    }

    private List<ClientDto> prepareClients() {
        ObjectMapper mapper = new ObjectMapper();
        BaseValidator.assertNotEmpty(ClientDataInitializer.FILE_NAME, "File name cannot be empty.");
        try (InputStream inputStream = ClientDataInitializer.class.getClassLoader().getResourceAsStream(
                FILE_NAME)) {
            return mapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new SampleDataInitializingException(
                    String.format("There was a problem with adding clients from %1$s file", FILE_NAME), e);
        }
    }
}
