package com.arturjarosz.task.finance.rest;

import com.arturjarosz.task.finance.application.InstallmentApplicationService;
import com.arturjarosz.task.finance.application.dto.InstallmentDto;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
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

@RestController
@RequestMapping("projects")
public class InstallmentRestController {

    private final InstallmentApplicationService installmentApplicationService;

    public InstallmentRestController(InstallmentApplicationService installmentApplicationService) {
        this.installmentApplicationService = installmentApplicationService;
    }

    @PostMapping("{projectId}/stages/{stageId}/installments")
    public ResponseEntity<InstallmentDto> createInstallment(@PathVariable("projectId") Long projectId,
            @PathVariable("stageId") Long stageId, @RequestBody InstallmentDto installmentDto) {
        InstallmentDto createdInstallmentDto = this.installmentApplicationService.createInstallment(projectId, stageId,
                installmentDto);
        HttpHeaders headers = new HttpHeadersBuilder().withLocation("project/{projectId}/installments/{installmentId}",
                projectId, installmentDto.getId()).build();
        return new ResponseEntity<>(createdInstallmentDto, headers, HttpStatus.CREATED);
    }

    @PutMapping("{projectId}/installments/{installmentId}")
    public ResponseEntity<InstallmentDto> updateInstallment(@PathVariable("projectId") Long projectId,
            @PathVariable("installmentId") Long installmentId, @RequestBody InstallmentDto installmentDto) {
        return new ResponseEntity<>(
                this.installmentApplicationService.updateInstallment(projectId, installmentId, installmentDto),
                HttpStatus.OK);
    }

    @DeleteMapping("{projectId}/installments/{installmentId}")
    public ResponseEntity<Void> removeInstallment(@PathVariable("projectId") Long projectId,
            @PathVariable("installmentId") Long installmentId) {
        this.installmentApplicationService.removeInstallment(projectId, installmentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("{projectId}/installments/{installmentId}/pay")
    public ResponseEntity<InstallmentDto> payInstallment(@PathVariable("projectId") Long projectId,
            @PathVariable("installmentId") Long installmentId, @RequestBody InstallmentDto installmentDto) {
        return new ResponseEntity<>(
                this.installmentApplicationService.payInstallment(projectId, installmentId, installmentDto),
                HttpStatus.OK);
    }

    @GetMapping("{projectId}/installments/{installmentId}")
    public ResponseEntity<InstallmentDto> getInstallment(@PathVariable("projectId") Long projectId,
            @PathVariable("installmentId") Long installmentId) {
        return new ResponseEntity<>(this.installmentApplicationService.getInstallment(projectId, installmentId),
                HttpStatus.OK);
    }

    @GetMapping("{projectId}/installments")
    public ResponseEntity<List<InstallmentDto>> getProjectInstallments(@PathVariable("projectId") Long projectId) {
        return new ResponseEntity<>(this.installmentApplicationService.getProjectInstallments(projectId),
                HttpStatus.OK);
    }

}
