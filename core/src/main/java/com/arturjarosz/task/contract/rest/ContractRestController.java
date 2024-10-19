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
    public ResponseEntity<ContractDto> changeStatus(ContractDto contractDto, Long contractId) {
        return new ResponseEntity<>(this.contractService.changeStatus(contractId, contractDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ContractDto> updateContract(ContractDto contractDto, Long contractId) {
        return null;
    }

}
