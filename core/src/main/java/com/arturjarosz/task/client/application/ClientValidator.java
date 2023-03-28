package com.arturjarosz.task.client.application;

import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.client.domain.ClientExceptionCodes;
import com.arturjarosz.task.client.infrastructure.repository.ClientRepository;
import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.client.model.ClientType;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates Client entity and Client related Dtos.
 */
@RequiredArgsConstructor
@Component
public class ClientValidator {

    @NonNull
    private final ClientRepository clientRepository;
    @NonNull
    private final ProjectQueryService projectQueryService;

    public void validateClientDtoPresence(ClientDto clientDto) {
        assertIsTrue(clientDto != null, createMessageCode(ExceptionCodes.NULL, ClientExceptionCodes.CLIENT));
    }

    public void validateClientBasicDto(ClientDto clientDto) {
        assertIsTrue(clientDto != null, createMessageCode(ExceptionCodes.NULL, ClientExceptionCodes.CLIENT));
        assertIsTrue(clientDto.getClientType() != null,
                createMessageCode(ExceptionCodes.NULL, ClientExceptionCodes.CLIENT, ClientExceptionCodes.CLIENT_TYPE));
        if (clientDto.getClientType().equals(ClientType.CORPORATE)) {
            this.validateCorporateClient(clientDto);
        } else {
            this.validatePrivateClient(clientDto);
        }
    }

    public void validatePrivateClient(ClientDto clientDto) {
        this.validateName(clientDto.getFirstName(), ClientExceptionCodes.FIRST_NAME);
        this.validateName(clientDto.getLastName(), ClientExceptionCodes.LAST_NAME);
    }

    public void validateCorporateClient(ClientDto clientDto) {
        this.validateName(clientDto.getCompanyName(), ClientExceptionCodes.COMPANY_NAME);
    }

    public void validateClientExistence(Optional<Client> maybeClient, Long clientId) {
        assertIsTrue(maybeClient.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, ClientExceptionCodes.CLIENT), clientId);
    }

    private void validateName(String name, String nameExceptionCode) {
        assertIsTrue(name != null,
                createMessageCode(ExceptionCodes.NULL, ClientExceptionCodes.CLIENT, nameExceptionCode));
        assertNotEmpty(name, createMessageCode(ExceptionCodes.EMPTY, ClientExceptionCodes.CLIENT, nameExceptionCode));
    }

    public void validateClientExistence(Long clientId) {
        Optional<Client> maybeClient = this.clientRepository.findById(clientId);
        this.validateClientExistence(maybeClient, clientId);
    }

    public void validateClientHasNoProjects(Long clientId) {
        List<Project> projects = this.projectQueryService.getProjectsForClientId(clientId);
        assertIsTrue(projects.isEmpty(), createMessageCode(ExceptionCodes.NOT_VALID, ClientExceptionCodes.CLIENT,
                ClientExceptionCodes.PROJECTS));
    }

}
