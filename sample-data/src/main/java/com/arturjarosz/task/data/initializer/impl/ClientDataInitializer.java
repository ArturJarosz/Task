package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.dto.ClientDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(2)
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
        return SampleDataFileReader.readList(FILE_NAME, ClientDto.class);
    }
}
