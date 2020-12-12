package com.arturjarosz.task.client.domain;

import com.arturjarosz.task.client.application.dto.ClientBasicDto;
import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;

public interface ClientDomainService {

    /**
     * Creates client from basic data.
     *
     * @param clientBasicDto
     * @return
     */

    CreatedEntityDto createClient(ClientBasicDto clientBasicDto);

    /**
     * Gets only basic data of client.
     * If clients exists basic DTO is returned.
     * If there is no client with given id, an error is thrown.
     *
     * @param clientId
     * @return
     */
    ClientBasicDto getClientBasicData(Long clientId);

    /**
     * Removes client of given id.
     *
     * @param clientId
     */
    void removeClient(Long clientId);

    /**
     * Get full data of client.
     *
     * @param clientId
     * @return
     */
    ClientDto getClient(Long clientId);

    /**
     * If clients exists, updates its data.
     *
     * @param clientId
     * @param clientDto
     */
    void updateClient(Long clientId, ClientDto clientDto);

    /**
     * Get list of clients names and ids.
     *
     * @return
     */
    List<ClientBasicDto> getBasicClients();
}
