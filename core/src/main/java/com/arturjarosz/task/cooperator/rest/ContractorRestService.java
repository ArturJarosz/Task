package com.arturjarosz.task.cooperator.rest;

import com.arturjarosz.task.cooperator.application.ContractorApplicationService;
import com.arturjarosz.task.cooperator.application.dto.ContractorDto;
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
@RequestMapping("contractor")
public class ContractorRestService {

    private final ContractorApplicationService contractorApplicationService;

    public ContractorRestService(
            ContractorApplicationService contractorApplicationService) {
        this.contractorApplicationService = contractorApplicationService;
    }

    @PostMapping("")
    public ResponseEntity<CreatedEntityDto> createContractor(@RequestBody ContractorDto contractorDto) {
        return new ResponseEntity(this.contractorApplicationService.createContractor(contractorDto),
                HttpStatus.CREATED);
    }

    @PutMapping("{contractorId}")
    public ResponseEntity<Void> updateContractor(@PathVariable("contractorId") Long contractorId,
                                                 @RequestBody ContractorDto contractorDto) {
        this.contractorApplicationService.updateContractor(contractorId, contractorDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("{contractorId}")
    public ResponseEntity<Void> deleteContractor(@PathVariable("contractorId") Long contractorId) {
        this.contractorApplicationService.deleteContractor(contractorId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("{contractorId}")
    public ResponseEntity<ContractorDto> getContractor(@PathVariable("contractorId") Long contractorId) {
        return new ResponseEntity<>(this.contractorApplicationService.getContractor(contractorId), HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<ContractorDto>> getBasicContractors() {
        return new ResponseEntity<>(this.contractorApplicationService.getBasicContractors(), HttpStatus.OK);
    }
}
