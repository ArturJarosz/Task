package com.arturjarosz.task.client.rest;

import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.client.application.dto.ClientBasicDto;
import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
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

@RestController
@RequestMapping("clients")
public class ClientRestController {

    private final ClientApplicationService clientApplicationService;

    public ClientRestController(
            ClientApplicationService clientApplicationService) {
        this.clientApplicationService = clientApplicationService;
    }

    @PostMapping("")
    public ResponseEntity<CreatedEntityDto> createClient(@RequestBody ClientBasicDto clientBasicDto) {
        return new ResponseEntity(this.clientApplicationService.createClient(clientBasicDto), HttpStatus.CREATED);
    }

    @DeleteMapping("{clientId}")
    public ResponseEntity<Void> removeClient(@PathVariable("clientId") Long clientId) {
        this.clientApplicationService.removeClient(clientId);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("{clientId}")
    public ResponseEntity<ClientDto> getClient(@PathVariable("clientId") Long clientId) {
        return new ResponseEntity<>(this.clientApplicationService.getClient(clientId), HttpStatus.OK);
    }

    @PutMapping("{clientId}")
    public ResponseEntity<Void> updateClient(@PathVariable("clientId") Long clientId,
                                             @RequestBody ClientDto clientDto) {
        this.clientApplicationService.updateClient(clientId, clientDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<ClientBasicDto>> getBasicClients() {
        return new ResponseEntity<>(this.clientApplicationService.getBasicClients(), HttpStatus.OK);
    }

}
