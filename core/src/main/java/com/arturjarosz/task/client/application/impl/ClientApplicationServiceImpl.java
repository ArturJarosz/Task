package com.arturjarosz.task.client.application.impl;

import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.client.application.ClientValidator;
import com.arturjarosz.task.client.application.mapper.ClientMapper;
import com.arturjarosz.task.client.infrastructure.repository.ClientRepository;
import com.arturjarosz.task.dto.ClientDto;
import com.arturjarosz.task.dto.ClientTypeDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ClientApplicationServiceImpl implements ClientApplicationService {
    @NonNull
    private final ClientRepository clientRepository;
    @NonNull
    private final ClientValidator clientValidator;
    @NonNull
    private final ClientMapper clientMapper;

    @Transactional
    @Override
    public ClientDto createClient(ClientDto clientDto) {
        LOG.debug("Creating client.");

        this.clientValidator.validateClientBasicDto(clientDto);
        var clientType = clientDto.getClientType();

        if (clientType == ClientTypeDto.CORPORATE) {
            this.clientValidator.validateCorporateClient(clientDto);
        } else {
            this.clientValidator.validatePrivateClient(clientDto);
        }

        var client = this.clientMapper.clientDtoToClient(clientDto);
        client = this.clientRepository.save(client);

        LOG.debug("Client created");
        return this.clientMapper.mapToDto(client);
    }

    @Transactional
    @Override
    public void removeClient(Long clientId) {
        LOG.debug("Removing Client with id {}.", clientId);

        this.clientValidator.validateClientExistence(clientId);
        this.clientValidator.validateClientHasNoProjects(clientId);
        this.clientRepository.deleteById(clientId);

        LOG.debug("Client with id {} removed.", clientId);
    }

    @Override
    public ClientDto getClient(Long clientId) {
        LOG.debug("Loading Client with id {}", clientId);

        var maybeClient = this.clientRepository.findById(clientId);
        this.clientValidator.validateClientExistence(maybeClient, clientId);

        LOG.debug("Client with id {} loaded.", clientId);
        return this.clientMapper.mapToDto(maybeClient.orElseThrow(ResourceNotFoundException::new));
    }

    @Transactional
    @Override
    public ClientDto updateClient(Long clientId, ClientDto clientDto) {
        LOG.debug("Updating Client with id {}.", clientId);

        var maybeClient = this.clientRepository.findById(clientId);
        this.clientValidator.validateClientExistence(maybeClient, clientId);
        var client = maybeClient.orElseThrow(ResourceNotFoundException::new);
        this.clientValidator.validateClientDtoPresence(clientDto);

        if (client.isPrivate()) {
            this.clientValidator.validatePrivateClient(clientDto);
            this.clientMapper.updatePrivateClientFromDto(clientDto, client);
        } else {
            this.clientValidator.validateCorporateClient(clientDto);
            this.clientMapper.updateCorporateClientFromDto(clientDto, client);
        }

        this.clientRepository.save(client);

        LOG.debug("Client with id {} updated.", clientId);
        return this.clientMapper.mapToDto(client);
    }

    @Override
    public List<ClientDto> getClients() {
        return this.clientRepository.findAll().stream().map(this.clientMapper::mapToDto).toList();
    }

    @Override
    public ClientDto getClientBasicData(Long clientId) {
        var maybeClient = this.clientRepository.findById(clientId);
        this.clientValidator.validateClientExistence(maybeClient, clientId);
        return this.clientMapper.mapToDto(maybeClient.orElseThrow(ResourceNotFoundException::new));
    }
}
