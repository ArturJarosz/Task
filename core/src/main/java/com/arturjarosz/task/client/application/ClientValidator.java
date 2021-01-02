package com.arturjarosz.task.client.application;

import com.arturjarosz.task.client.application.dto.ClientBasicDto;
import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.client.domain.ClientExceptionCodes;
import com.arturjarosz.task.client.infrastructure.repository.ClientRepository;
import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.client.model.ClientType;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates Client entity and Client related Dtos.
 */
@Component
public class ClientValidator {

    private final ClientRepository clientRepository;

    public ClientValidator(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public static void validateClientDtoPresence(ClientDto clientDto) {
        assertIsTrue(clientDto != null,
                createMessageCode(ExceptionCodes.IS_NULL, ClientExceptionCodes.CLIENT));
    }

    public static void validateClientBasicDto(ClientBasicDto clientBasicDto) {
        assertIsTrue(clientBasicDto != null,
                createMessageCode(ExceptionCodes.IS_NULL, ClientExceptionCodes.CLIENT));
        assertIsTrue(clientBasicDto.getClientType() != null,
                createMessageCode(ExceptionCodes.IS_NULL, ClientExceptionCodes.CLIENT,
                        ClientExceptionCodes.CLIENT_TYPE));
        if (clientBasicDto.getClientType().equals(ClientType.CORPORATE)) {
            validateCorporateClient(clientBasicDto);
        } else {
            validatePrivateClient(clientBasicDto);
        }
    }

    public static void validatePrivateClient(ClientBasicDto clientBasicDto) {
        validateName(clientBasicDto.getFirstName(), ClientExceptionCodes.FIRST_NAME);
        validateName(clientBasicDto.getLastName(), ClientExceptionCodes.LAST_NAME);
    }

    public static void validatePrivateClient(ClientDto clientDto) {
        validateName(clientDto.getFirstName(), ClientExceptionCodes.FIRST_NAME);
        validateName(clientDto.getLastName(), ClientExceptionCodes.LAST_NAME);
    }

    public static void validateCorporateClient(ClientBasicDto clientBasicDto) {
        validateName(clientBasicDto.getCompanyName(), ClientExceptionCodes.COMPANY_NAME);
    }

    public static void validateCorporateClient(ClientDto clientDto) {
        validateName(clientDto.getCompanyName(), ClientExceptionCodes.COMPANY_NAME);
    }

    public static void validateClientExistence(Client client, Long clientId) {
        assertIsTrue(client != null,
                createMessageCode(ExceptionCodes.NOT_EXISTS, ClientExceptionCodes.CLIENT), clientId);
    }

    private static void validateName(String name, String nameExceptionCode) {
        assertIsTrue(name != null,
                createMessageCode(ExceptionCodes.IS_NULL, ClientExceptionCodes.CLIENT,
                        nameExceptionCode));
        assertNotEmpty(name, createMessageCode(ExceptionCodes.EMPTY, ClientExceptionCodes.CLIENT, nameExceptionCode));
    }

    public void validateClientExistence(Long clientId) {
        Client client = this.clientRepository.load(clientId);
        validateClientExistence(client, clientId);
    }

}
