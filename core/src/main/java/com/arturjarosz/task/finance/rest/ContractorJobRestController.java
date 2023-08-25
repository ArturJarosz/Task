package com.arturjarosz.task.finance.rest;

import com.arturjarosz.task.dto.ContractorJobDto;
import com.arturjarosz.task.finance.application.ContractorJobApplicationService;
import com.arturjarosz.task.rest.ContractorJobApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ContractorJobRestController implements ContractorJobApi {

    @NonNull
    private final ContractorJobApplicationService contractorJobApplicationService;

    @Override
    public ResponseEntity<ContractorJobDto> createContractorJob(ContractorJobDto contractorJobDto, Long projectId) {
        var createdContractorJobDto = this.contractorJobApplicationService.createContractorJob(projectId, contractorJobDto);
        var headers = new HttpHeadersBuilder().withLocation("/projects/{projectId}/contractor-jobs/{contractorJobId}", projectId, createdContractorJobDto.getId()).build();
        return new ResponseEntity<>(createdContractorJobDto, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteContractorJob(Long projectId, Long contractorJobId) {
        this.contractorJobApplicationService.deleteContractorJob(projectId, contractorJobId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ContractorJobDto> updateContractorJob(ContractorJobDto contractorJobDto, Long projectId, Long contractorJobId) {
        return new ResponseEntity<>(this.contractorJobApplicationService.updateContractorJob(projectId, contractorJobId, contractorJobDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ContractorJobDto> getContractorJob(Long projectId, Long contractorJobId) {
        return new ResponseEntity<>(this.contractorJobApplicationService.getContractorJob(projectId, contractorJobId), HttpStatus.OK);
    }
}
