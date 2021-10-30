package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.project.application.InstallmentApplicationService;
import com.arturjarosz.task.project.application.dto.InstallmentDto;
import com.arturjarosz.task.sharedkernel.utils.HttpHeadersBuilder;
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
                                                            @PathVariable("stageId") Long stageId,
                                                            @RequestBody InstallmentDto installmentDto) {
        InstallmentDto createdInstallmentDto = this.installmentApplicationService
                .createInstallment(projectId, stageId, installmentDto);
        HttpHeaders headers = new HttpHeadersBuilder()
                .withLocation("project/{projectId}/stages/{stageId}/installments/{installmentId}", projectId, stageId,
                        installmentDto.getId())
                .build();
        return new ResponseEntity<>(createdInstallmentDto, headers, HttpStatus.CREATED);
    }

    @PutMapping("{projectId}/stages/{stageId}/installments")
    public ResponseEntity<InstallmentDto> updateInstallment(@PathVariable("projectId") Long projectId,
                                                            @PathVariable("stageId") Long stageId,
                                                            @RequestBody InstallmentDto installmentDto) {
        return new ResponseEntity<>(
                this.installmentApplicationService.updateInstallment(projectId, stageId, installmentDto),
                HttpStatus.OK);
    }

    @DeleteMapping("{projectId}/stages/{stageId}/installments")
    public ResponseEntity<Void> removeInstallment(@PathVariable("projectId") Long projectId,
                                                  @PathVariable("stageId") Long stageId) {
        this.installmentApplicationService.removeInstallment(projectId, stageId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("{projectId}/stages/{stageId}/installments/pay")
    public ResponseEntity<InstallmentDto> payInstallment(@PathVariable("projectId") Long projectId,
                                                         @PathVariable("stageId") Long stageId,
                                                         @RequestBody InstallmentDto installmentDto) {
        return new ResponseEntity<>(
                this.installmentApplicationService.payInstallment(projectId, stageId, installmentDto), HttpStatus.OK);
    }

    @GetMapping("{projectId}/stages/{stageId}/installments")
    public ResponseEntity<InstallmentDto> getInstallment(@PathVariable("projectId") Long projectId,
                                                         @PathVariable("stageId") Long stageId) {
        return new ResponseEntity<>(this.installmentApplicationService.getInstallment(projectId, stageId),
                HttpStatus.OK);
    }

    @GetMapping("{projectId}/stages/installments")
    public ResponseEntity<List<InstallmentDto>> getInstallmentList(@PathVariable("projectId") Long projectId) {
        return new ResponseEntity<>(this.installmentApplicationService.getInstallmentList(projectId), HttpStatus.OK);
    }

    // get all installments for project
}
