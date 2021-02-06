package com.arturjarosz.task.client.application.impl;

import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.client.application.ClientValidator;
import com.arturjarosz.task.client.application.dto.ClientBasicDto;
import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.client.application.mapper.ClientDtoMapper;
import com.arturjarosz.task.client.infrastructure.repository.ClientRepository;
import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.client.model.ClientType;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.arturjarosz.task.client.application.ClientValidator.validateClientBasicDto;
import static com.arturjarosz.task.client.application.ClientValidator.validateClientDtoPresence;
import static com.arturjarosz.task.client.application.ClientValidator.validateClientExistence;
import static com.arturjarosz.task.client.application.ClientValidator.validateCorporateClient;
import static com.arturjarosz.task.client.application.ClientValidator.validatePrivateClient;

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
    public CreatedEntityDto createClient(ClientBasicDto clientBasicDto) {
        LOG.debug("creating client");

        validateClientBasicDto(clientBasicDto);
        ClientType clientType = clientBasicDto.getClientType();
        Client client;
        if (clientType.equals(ClientType.CORPORATE)) {
            client = Client.createCorporateClient(clientBasicDto.getCompanyName());
        } else {
            client = Client.createPrivateClient(clientBasicDto.getFirstName(),
                    clientBasicDto.getLastName());
        }
        client = this.clientRepository.save(client);

        LOG.debug("client created");
        return new CreatedEntityDto(client.getId());
    }

    @Transactional
    @Override
    public void removeClient(Long clientId) {
        //TODO: check is client has project - if has, can't be removed
        LOG.debug("removing client with id {}", clientId);

        this.clientValidator.validateClientExistence(clientId);
        this.clientRepository.remove(clientId);

        LOG.debug("client with id {} removed", clientId);
    }

    @Override
    public ClientDto getClient(Long clientId) {
        Client client = this.clientRepository.load(clientId);
        validateClientExistence(client, clientId);

        LOG.debug("client with id {} loaded", clientId);
        return ClientDtoMapper.INSTANCE.clientToClientDto(client);
    }

    @Transactional
    @Override
    public void updateClient(Long clientId, ClientDto clientDto) {
        LOG.debug("updating client with id {}", clientId);

        Client client = this.clientRepository.load(clientId);
        validateClientExistence(client, clientId);
        validateClientDtoPresence(clientDto);
        if (client.isPrivate()) {
            validatePrivateClient(clientDto);
            client.updatePersonName(clientDto.getFirstName(), clientDto.getLastName());
        } else {
            validateCorporateClient(clientDto);
            client.updateCompanyName(clientDto.getCompanyName());
        }
        if (clientDto.getContact() != null) {
            if (clientDto.getContact().getAddress() != null) {
                client.updateAddress(
                        ClientDtoMapper.INSTANCE.addressDtoToAddress(clientDto.getContact().getAddress()));
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

        LOG.debug("client with id {} updated", clientId);
    }

    @Override
    public List<ClientBasicDto> getBasicClients() {
        return this.clientRepository.loadAll()
                .stream()
                .map(ClientDtoMapper.INSTANCE::clientToClientBasicDto)
                .collect(Collectors.toList());
    }

    @Override
    public ClientBasicDto getClientBasicData(Long clientId) {
        Client client = this.clientRepository.load(clientId);
        validateClientExistence(client, clientId);
        return ClientDtoMapper.INSTANCE.clientToClientBasicDto(client);
    }
}
