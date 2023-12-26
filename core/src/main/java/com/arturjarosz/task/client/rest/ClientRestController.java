package com.arturjarosz.task.client.rest;

import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.dto.ClientDto;
import com.arturjarosz.task.rest.ClientApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ClientRestController implements ClientApi {
    private static final String CLIENTS_API = "/clients";

    @NonNull
    private final ClientApplicationService clientApplicationService;

    @Override
    public ResponseEntity<ClientDto> createClient(ClientDto clientDto) {
        var createdClientDto = this.clientApplicationService.createClient(clientDto);
        var headers = new HttpHeadersBuilder()
                .withLocation("%s/{id}".formatted(CLIENTS_API), createdClientDto.getId())
                .build();
        return new ResponseEntity<>(createdClientDto, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteClient(Long clientId) {
        this.clientApplicationService.removeClient(clientId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ClientDto> getClient(Long clientId) {
        return new ResponseEntity<>(this.clientApplicationService.getClient(clientId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ClientDto> updateClient(ClientDto clientDto, Long clientId) {
        return new ResponseEntity<>(this.clientApplicationService.updateClient(clientId, clientDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ClientDto>> getClients() {
        return new ResponseEntity<>(this.clientApplicationService.getClients(), HttpStatus.OK);
    }

}
