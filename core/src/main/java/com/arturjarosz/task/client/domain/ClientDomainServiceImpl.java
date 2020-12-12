package com.arturjarosz.task.client.domain;

import com.arturjarosz.task.client.application.dto.ClientBasicDto;
import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.client.application.mapper.ClientDtoMapper;
import com.arturjarosz.task.client.infrastructure.repository.ClientRepository;
import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.client.model.ClientType;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;
import java.util.stream.Collectors;

import static com.arturjarosz.task.client.domain.ClientValidator.validateClientBasicDto;
import static com.arturjarosz.task.client.domain.ClientValidator.validateClientDtoExistance;
import static com.arturjarosz.task.client.domain.ClientValidator.validateClientExistence;
import static com.arturjarosz.task.client.domain.ClientValidator.validateCorporateClient;
import static com.arturjarosz.task.client.domain.ClientValidator.validatePrivateClient;

@DomainService
public class ClientDomainServiceImpl implements ClientDomainService {

    private final ClientRepository clientRepository;

    public ClientDomainServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public CreatedEntityDto createClient(ClientBasicDto clientBasicDto) {
        validateClientBasicDto(clientBasicDto);
        ClientType clientType = clientBasicDto.getClientType();
        Client client;
        if (clientType.equals(ClientType.CORPORATE)) {
            client = Client.createCorporateClient(clientBasicDto.getCompanyName());
        } else {
            client = Client.createPrivateClient(clientBasicDto.getFirstName(),
                    clientBasicDto.getLastName());
        }
        this.clientRepository.save(client);
        return new CreatedEntityDto(client.getId());
    }

    @Override
    public void removeClient(Long clientId) {
        this.clientRepository.remove(clientId);
    }

    @Override
    public ClientDto getClient(Long clientId) {
        Client client = this.clientRepository.load(clientId);
        validateClientExistence(client, clientId);
        return ClientDtoMapper.INSTANCE.clientToClientDto(client);
    }

    @Override
    public void updateClient(Long clientId, ClientDto clientDto) {
        Client client = this.clientRepository.load(clientId);
        validateClientExistence(client, clientId);
        validateClientDtoExistance(clientDto);
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
        BaseValidator.assertIsTrue(client != null,
                BaseValidator.createMessageCode(ExceptionCodes.NOT_EXISTS, ClientExceptionCodes.CLIENT), clientId);
        return ClientDtoMapper.INSTANCE.clientToClientBasicDto(client);
    }
}
