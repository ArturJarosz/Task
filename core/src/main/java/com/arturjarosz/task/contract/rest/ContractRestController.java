package com.arturjarosz.task.contract.rest;

import com.arturjarosz.task.contract.application.ContractService;
import com.arturjarosz.task.dto.ContractDto;
import com.arturjarosz.task.rest.ContractApi;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ContractRestController implements ContractApi {
    @NonNull
    private final ContractService contractService;

    @Override
    public ResponseEntity<ContractDto> rejectOffer(Long contractId) {
        return new ResponseEntity<>(this.contractService.reject(contractId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ContractDto> makeNewOffer(ContractDto contractDto, Long contractId) {
        return new ResponseEntity<>(this.contractService.makeNewOffer(contractId, contractDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ContractDto> acceptOffer(Long contractId) {
        return new ResponseEntity<>(this.contractService.acceptOffer(contractId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ContractDto> sign(ContractDto contractDto, Long contractId) {
        return new ResponseEntity<>(this.contractService.sign(contractId, contractDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ContractDto> terminate(ContractDto contractDto, Long contractId) {
        return new ResponseEntity<>(this.contractService.terminate(contractId, contractDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ContractDto> resume(Long contractId) {
        return new ResponseEntity<>(this.contractService.resume(contractId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ContractDto> complete(ContractDto contractDto, Long contractId) {
        return new ResponseEntity<>(this.contractService.complete(contractId, contractDto), HttpStatus.OK);
    }

}
