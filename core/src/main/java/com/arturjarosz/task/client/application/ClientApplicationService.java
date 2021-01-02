package com.arturjarosz.task.client.application;

import com.arturjarosz.task.client.application.dto.ClientBasicDto;
import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;

public interface ClientApplicationService {

    CreatedEntityDto createClient(ClientBasicDto clientBasicDto);

    ClientBasicDto getClientBasicData(Long clientId);

    void removeClient(Long clientId);

    ClientDto getClient(Long clientId);

    void updateClient(Long clientId, ClientDto clientDto);

    List<ClientBasicDto> getBasicClients();
}
