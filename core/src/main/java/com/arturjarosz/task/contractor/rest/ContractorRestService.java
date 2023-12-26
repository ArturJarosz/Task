package com.arturjarosz.task.contractor.rest;

import com.arturjarosz.task.contractor.application.ContractorApplicationService;
import com.arturjarosz.task.dto.ContractorDto;
import com.arturjarosz.task.rest.ContractorApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ContractorRestService implements ContractorApi {
    private static final String CONTRACTORS_API = "/contractors";

    @NonNull
    private final ContractorApplicationService contractorApplicationService;

    @Override
    public ResponseEntity<ContractorDto> createContractor(ContractorDto contractorDto) {
        var createdContractor = this.contractorApplicationService.createContractor(contractorDto);
        var headers = new HttpHeadersBuilder().withLocation("%s/{contractorId}".formatted(CONTRACTORS_API), createdContractor.getId()).build();
        return new ResponseEntity<>(createdContractor, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ContractorDto> updateContractor(ContractorDto contractorDto, Long contractorId) {
        var updatedContractor = this.contractorApplicationService.updateContractor(contractorId, contractorDto);
        return new ResponseEntity<>(updatedContractor, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteContractor(Long contractorId) {
        this.contractorApplicationService.deleteContractor(contractorId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ContractorDto> getContractor(Long contractorId) {
        return new ResponseEntity<>(this.contractorApplicationService.getContractor(contractorId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ContractorDto>> getContractors() {
        return new ResponseEntity<>(this.contractorApplicationService.getBasicContractors(), HttpStatus.OK);
    }
}
