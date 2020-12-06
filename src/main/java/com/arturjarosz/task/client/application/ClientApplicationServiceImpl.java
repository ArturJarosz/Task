package com.arturjarosz.task.client.application;

import com.arturjarosz.task.client.application.dto.ClientBasicDto;
import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.client.domain.ClientDomainService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import javax.transaction.Transactional;
import java.util.List;

@ApplicationService
public class ClientApplicationServiceImpl implements ClientApplicationService {
    private final ClientDomainService clientDomainService;

    public ClientApplicationServiceImpl(ClientDomainService clientDomainService) {
        this.clientDomainService = clientDomainService;
    }

    @Transactional
    @Override
    public CreatedEntityDto createClient(ClientBasicDto clientBasicDto) {
        return this.clientDomainService.createClient(clientBasicDto);
    }

    @Transactional
    @Override
    public void deleteClient(Long clientId) {
        //TODO: check is client has project - if has, can't be deleted
        this.clientDomainService.removeClient(clientId);
    }

    @Override
    public ClientDto getClient(Long clientId) {
        return this.clientDomainService.getClient(clientId);
    }

    @Transactional
    @Override
    public void updateClient(Long clientId, ClientDto clientDto) {
        this.clientDomainService.updateClient(clientId, clientDto);
    }

    @Override
    public List<ClientBasicDto> getBasicClients() {
        return this.clientDomainService.getBasicClients();
    }

    @Override
    public ClientBasicDto getClientBasicData(Long clientId) {
        return this.clientDomainService.getClientBasicData(clientId);
    }
}
