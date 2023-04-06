package com.arturjarosz.task.client.application;

import com.arturjarosz.task.client.application.dto.ClientDto;

import java.util.List;

public interface ClientApplicationService {

    /**
     * Creates {@link com.arturjarosz.task.client.model.Client} from given {@link ClientDto}.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    ClientDto createClient(ClientDto clientDto);

    /**
     * Loads basic data for {@link com.arturjarosz.task.client.model.Client} by given clientId.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    ClientDto getClientBasicData(Long clientId);

    /**
     * Removes {@link com.arturjarosz.task.client.model.Client} by given clientId.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    void removeClient(Long clientId);

    /**
     * Loads all data for {@link com.arturjarosz.task.client.model.Client} by given clientId.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    ClientDto getClient(Long clientId);

    /**
     * Updates {@link com.arturjarosz.task.client.model.Client} of given clientId be data provided in ClientDto.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    ClientDto updateClient(Long clientId, ClientDto clientDto);

    /**
     * Loads list of basic clients data or all existing clients.
     */
    List<ClientDto> getBasicClients();
}
