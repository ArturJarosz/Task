package com.arturjarosz.task.client.application.impl;

import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.client.application.ClientValidator;
import com.arturjarosz.task.client.application.mapper.ClientMapper;
import com.arturjarosz.task.client.infrastructure.repository.ClientRepository;
import com.arturjarosz.task.contract.query.ContractQueryService;
import com.arturjarosz.task.dto.ClientDto;
import com.arturjarosz.task.dto.ClientTypeDto;
import com.arturjarosz.task.dto.EntityProjectsSummaryDto;
import com.arturjarosz.task.project.application.mapper.ProjectsToEntityProjectsSummaryDtoMapper;
import com.arturjarosz.task.project.query.ProjectCounts;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.properties.ProjectProperties;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ClientApplicationServiceImpl implements ClientApplicationService {

    private final ClientRepository clientRepository;
    private final ClientValidator clientValidator;
    private final ClientMapper clientMapper;
    private final ProjectQueryService projectQueryService;
    private final ContractQueryService contractQueryService;
    private final ProjectsToEntityProjectsSummaryDtoMapper projectsToEntityProjectsSummaryDtoMapper;
    private final ProjectProperties projectProperties;

    @Transactional
    @Override
    public ClientDto createClient(ClientDto clientDto) {
        LOG.info("Creating client.");

        this.clientValidator.validateClientBasicDto(clientDto);
        var clientType = clientDto.getClientType();

        if (clientType == ClientTypeDto.CORPORATE) {
            this.clientValidator.validateCorporateClient(clientDto);
        } else {
            this.clientValidator.validatePrivateClient(clientDto);
        }

        var client = this.clientMapper.clientDtoToClient(clientDto);
        client = this.clientRepository.save(client);

        LOG.info("Client created");
        return this.clientMapper.mapToDto(client);
    }

    @Transactional
    @Override
    public void removeClient(Long clientId) {
        LOG.info("Removing Client with id {}.", clientId);

        this.clientValidator.validateClientExistence(clientId);
        this.clientValidator.validateClientHasNoProjects(clientId);
        this.clientRepository.deleteById(clientId);

        LOG.info("Client with id {} removed.", clientId);
    }

    @Override
    public ClientDto getClient(Long clientId) {
        LOG.info("Loading Client with id {}", clientId);

        var maybeClient = this.clientRepository.findById(clientId);
        this.clientValidator.validateClientExistence(maybeClient, clientId);

        LOG.info("Client with id {} loaded.", clientId);
        return this.withProjectsData(
                List.of(this.clientMapper.mapToDto(maybeClient.orElseThrow(ResourceNotFoundException::new)))).get(0);
    }

    @Transactional
    @Override
    public ClientDto updateClient(Long clientId, ClientDto clientDto) {
        LOG.info("Updating Client with id {}.", clientId);

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

        LOG.info("Client with id {} updated.", clientId);
        return this.clientMapper.mapToDto(client);
    }

    @Override
    public List<ClientDto> getClients() {
        LOG.info("Getting all clients.");
        return this.withProjectsData(
                this.clientRepository.findAll().stream().map(this.clientMapper::mapToDto).toList());
    }

    @Override
    public EntityProjectsSummaryDto getClientProjectsSummary(Long clientId) {
        LOG.info("Getting projects summary for client with id {}.", clientId);
        this.clientValidator.validateClientExistence(clientId);

        var projects = this.projectQueryService.getProjectsForClientId(clientId);
        var contractValueByProjectId = this.contractQueryService.getContractValuesForProjectsByClientId(clientId);
        return this.projectsToEntityProjectsSummaryDtoMapper.mapToProjectSummaryDto(projects, contractValueByProjectId);
    }

    @Override
    public ClientDto getClientBasicData(Long clientId) {
        LOG.info("Getting basic data for client with id {}.", clientId);
        var maybeClient = this.clientRepository.findById(clientId);
        this.clientValidator.validateClientExistence(maybeClient, clientId);
        return this.clientMapper.mapToDto(maybeClient.orElseThrow(ResourceNotFoundException::new));
    }

    private List<ClientDto> withProjectsData(List<ClientDto> clientDtos) {
        var clientIds = clientDtos.stream().map(ClientDto::getId).toList();
        var countsByClientId = this.projectQueryService.getProjectCountsByClientIds(clientIds,
                this.projectProperties.activeStatuses());
        var valueByClientId = this.contractQueryService.getProjectsValueByClientIds(clientIds);
        clientDtos.forEach(clientDto -> {
            var counts = countsByClientId.getOrDefault(clientDto.getId(), ProjectCounts.NONE);
            clientDto.setNumberOfProjects((int) counts.all());
            clientDto.setNumberOfActiveProjects((int) counts.active());
            clientDto.setProjectsValue(valueByClientId.getOrDefault(clientDto.getId(), BigDecimal.ZERO).doubleValue());
        });
        return clientDtos;
    }
}
