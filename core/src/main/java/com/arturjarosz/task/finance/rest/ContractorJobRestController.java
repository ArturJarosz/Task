package com.arturjarosz.task.finance.rest;

import com.arturjarosz.task.finance.application.ContractorJobApplicationService;
import com.arturjarosz.task.finance.application.dto.ContractorJobDto;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("projects")
public class ContractorJobRestController {

    @NonNull
    private final ContractorJobApplicationService contractorJobApplicationService;

    @PostMapping("{projectId}/contractorJobs")
    public ResponseEntity<ContractorJobDto> createContractorJob(@PathVariable("projectId") Long projectId,
            @RequestBody ContractorJobDto contractorJobDto) {
        ContractorJobDto createdContractorJobDto = this.contractorJobApplicationService.createContractorJob(projectId,
                contractorJobDto);
        HttpHeaders headers = new HttpHeadersBuilder().withLocation(
                        "/projects/{projectId}/contractorJobs/{contractorJobId}", projectId, createdContractorJobDto.getId())
                .build();
        return new ResponseEntity<>(createdContractorJobDto, headers, HttpStatus.CREATED);
    }

    @DeleteMapping("{projectId}/contractorJobs/{contractorJobId}")
    public ResponseEntity<Void> deleteContractorJob(@PathVariable("projectId") Long projectId,
            @PathVariable("contractorJobId") Long contractorJobId) {
        this.contractorJobApplicationService.deleteContractorJob(projectId, contractorJobId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("{projectId}/contractorJobs/{contractorJobId}")
    public ResponseEntity<ContractorJobDto> updateContractorJob(@PathVariable("projectId") Long projectId,
            @PathVariable("contractorJobId") Long contractorJobId, @RequestBody ContractorJobDto contractorJobDto) {
        return new ResponseEntity<>(
                this.contractorJobApplicationService.updateContractorJob(projectId, contractorJobId, contractorJobDto),
                HttpStatus.OK);
    }

    @GetMapping("{projectId}/contractorJobs/{contractorJobId}")
    public ResponseEntity<ContractorJobDto> getContractorJob(@PathVariable("projectId") Long projectId,
            @PathVariable("contractorJobId") Long contractorJobId) {
        return new ResponseEntity<>(this.contractorJobApplicationService.getContractorJob(projectId, contractorJobId),
                HttpStatus.OK);
    }
}
