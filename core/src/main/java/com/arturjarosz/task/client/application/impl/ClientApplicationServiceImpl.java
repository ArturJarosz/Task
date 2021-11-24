package com.arturjarosz.task.client.application.impl;

import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.client.application.ClientValidator;
import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.client.application.mapper.ClientDtoMapper;
import com.arturjarosz.task.client.infrastructure.repository.ClientRepository;
import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.client.model.ClientType;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationService
public class ClientApplicationServiceImpl implements ClientApplicationService {
    private static final Logger LOG = LoggerFactory.getLogger(ClientApplicationServiceImpl.class);

    private final ClientRepository clientRepository;
    private final ClientValidator clientValidator;

    public ClientApplicationServiceImpl(ClientRepository clientRepository, ClientValidator clientValidator) {
        this.clientRepository = clientRepository;
        this.clientValidator = clientValidator;
    }

    @Transactional
    @Override
    public ClientDto createClient(ClientDto clientDto) {
        LOG.debug("Creating client.");

        this.clientValidator.validateClientBasicDto(clientDto);
        ClientType clientType = clientDto.getClientType();
        Client client;
        if (clientType.equals(ClientType.CORPORATE)) {
            client = Client.createCorporateClient(clientDto.getCompanyName());
        } else {
            client = Client.createPrivateClient(clientDto.getFirstName(), clientDto.getLastName());
        }
        client = this.clientRepository.save(client);

        LOG.debug("Client created");
        return ClientDtoMapper.INSTANCE.clientToClientDto(client);
    }

    @Transactional
    @Override
    public void removeClient(Long clientId) {
        LOG.debug("Removing Client with id {}.", clientId);

        this.clientValidator.validateClientExistence(clientId);
        this.clientValidator.validateClientHasNoProjects(clientId);
        this.clientRepository.remove(clientId);

        LOG.debug("Client with id {} removed.", clientId);
    }

    @Override
    public ClientDto getClient(Long clientId) {
        LOG.debug("Loading Client with id {}", clientId);

        Client client = this.clientRepository.load(clientId);
        this.clientValidator.validateClientExistence(client, clientId);

        LOG.debug("Client with id {} loaded.", clientId);
        return ClientDtoMapper.INSTANCE.clientToClientDto(client);
    }

    @Transactional
    @Override
    public ClientDto updateClient(Long clientId, ClientDto clientDto) {
        LOG.debug("Updating Client with id {}.", clientId);

        this.clientValidator.validateClientExistence(clientId);
        Client client = this.clientRepository.load(clientId);
        this.clientValidator.validateClientDtoPresence(clientDto);
        if (client.isPrivate()) {
            this.clientValidator.validatePrivateClient(clientDto);
            client.updatePersonName(clientDto.getFirstName(), clientDto.getLastName());
        } else {
            this.clientValidator.validateCorporateClient(clientDto);
            client.updateCompanyName(clientDto.getCompanyName());
        }
        if (clientDto.getContact() != null) {
            if (clientDto.getContact().getAddress() != null) {
                client.updateAddress(ClientDtoMapper.INSTANCE.addressDtoToAddress(clientDto.getContact().getAddress()));
            }
            if (clientDto.getContact().getEmail() != null) {
                client.updateEmail(clientDto.getContact().getEmail());
            }
            client.updateTelephone(clientDto.getContact().getTelephone());
        }
        if (clientDto.getAdditionalData() != null) {
            client.updateNote(clientDto.getAdditionalData().getNote());
        }
        this.clientRepository.save(client);

        LOG.debug("Client with id {} updated.", clientId);
        return ClientDtoMapper.INSTANCE.clientToClientDto(client);
    }

    @Override
    public List<ClientDto> getBasicClients() {
        return this.clientRepository.loadAll().stream().map(ClientDtoMapper.INSTANCE::clientToClientBasicDto)
                .collect(Collectors.toList());
    }

    @Override
    public ClientDto getClientBasicData(Long clientId) {
        Client client = this.clientRepository.load(clientId);
        this.clientValidator.validateClientExistence(client, clientId);
        return ClientDtoMapper.INSTANCE.clientToClientBasicDto(client);
    }
}
