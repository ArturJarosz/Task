package com.arturjarosz.task.contractor.rest;

import com.arturjarosz.task.contractor.application.ContractorApplicationService;
import com.arturjarosz.task.contractor.application.dto.ContractorDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("contractors")
public class ContractorRestService {

    private final ContractorApplicationService contractorApplicationService;

    public ContractorRestService(
            ContractorApplicationService contractorApplicationService) {
        this.contractorApplicationService = contractorApplicationService;
    }

    @PostMapping("")
    public ResponseEntity<CreatedEntityDto> createContractor(@RequestBody ContractorDto contractorDto) {
        return new ResponseEntity<>(this.contractorApplicationService.createContractor(contractorDto),
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

    @GetMapping("")
    public ResponseEntity<List<ContractorDto>> getBasicContractors() {
        return new ResponseEntity<>(this.contractorApplicationService.getBasicContractors(), HttpStatus.OK);
    }
}
