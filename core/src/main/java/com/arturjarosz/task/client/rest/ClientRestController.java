package com.arturjarosz.task.client.rest;

import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("clients")
public class ClientRestController {

    @NonNull
    private final ClientApplicationService clientApplicationService;

    @PostMapping("")
    public ResponseEntity<ClientDto> createClient(@RequestBody ClientDto clientDto) {
        ClientDto createdClientDto = this.clientApplicationService.createClient(clientDto);
        HttpHeaders headers = new HttpHeadersBuilder()
                .withLocation("/clients/{id}", createdClientDto.getId())
                .build();
        return new ResponseEntity<>(createdClientDto, headers, HttpStatus.CREATED);
    }

    @DeleteMapping("{clientId}")
    public ResponseEntity<Void> removeClient(@PathVariable("clientId") Long clientId) {
        this.clientApplicationService.removeClient(clientId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("{clientId}")
    public ResponseEntity<ClientDto> getClient(@PathVariable("clientId") Long clientId) {
        return new ResponseEntity<>(this.clientApplicationService.getClient(clientId), HttpStatus.OK);
    }

    @PutMapping("{clientId}")
    public ResponseEntity<ClientDto> updateClient(@PathVariable("clientId") Long clientId,
                                                  @RequestBody ClientDto clientDto) {
        return new ResponseEntity<>(this.clientApplicationService.updateClient(clientId, clientDto), HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<ClientDto>> getBasicClients() {
        return new ResponseEntity<>(this.clientApplicationService.getBasicClients(), HttpStatus.OK);
    }

}
