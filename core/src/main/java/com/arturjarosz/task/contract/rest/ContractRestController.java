package com.arturjarosz.task.contract.rest;

import com.arturjarosz.task.contract.application.ContractService;
import com.arturjarosz.task.contract.application.dto.ContractDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("contracts/{contractId}")
public class ContractRestController {
    private final ContractService contractService;

    @Autowired
    public ContractRestController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping("reject")
    public ResponseEntity<ContractDto> rejectOffer(@PathVariable("contractId") Long contractId) {
        return new ResponseEntity<>(this.contractService.reject(contractId), HttpStatus.OK);
    }


    @PostMapping("newOffer")
    public ResponseEntity<ContractDto> makeNewOffer(@PathVariable("contractId") Long contractId,
            @RequestBody ContractDto contractDto) {
        return new ResponseEntity<>(this.contractService.makeNewOffer(contractId, contractDto), HttpStatus.OK);
    }

    @PostMapping("acceptOffer")
    public ResponseEntity<ContractDto> acceptOffer(@PathVariable("contractId") Long contractId) {
        return new ResponseEntity<>(this.contractService.acceptOffer(contractId), HttpStatus.OK);
    }

    @PostMapping("sign")
    public ResponseEntity<ContractDto> sign(@PathVariable("contractId") Long contractId,
            @RequestBody ContractDto contractDto) {
        return new ResponseEntity<>(this.contractService.sign(contractId, contractDto), HttpStatus.OK);
    }

    @PostMapping("terminate")
    public ResponseEntity<ContractDto> terminate(@PathVariable("contractId") Long contractId,
            @RequestBody ContractDto contractDto) {
        return new ResponseEntity<>(this.contractService.terminate(contractId, contractDto), HttpStatus.OK);
    }

    @PostMapping("resume")
    public ResponseEntity<ContractDto> resume(@PathVariable("contractId") Long contractId) {
        return new ResponseEntity<>(this.contractService.resume(contractId), HttpStatus.OK);
    }

    @PostMapping("complete")
    public ResponseEntity<ContractDto> complete(@PathVariable("contractId") Long contractId,
            @RequestBody ContractDto contractDto) {
        return new ResponseEntity<>(this.contractService.complete(contractId, contractDto), HttpStatus.OK);
    }

}
