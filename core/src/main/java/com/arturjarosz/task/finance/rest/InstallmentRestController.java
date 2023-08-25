package com.arturjarosz.task.finance.rest;

import com.arturjarosz.task.dto.InstallmentDto;
import com.arturjarosz.task.finance.application.InstallmentApplicationService;
import com.arturjarosz.task.rest.InstallmentApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class InstallmentRestController implements InstallmentApi {

    @NonNull
    private final InstallmentApplicationService installmentApplicationService;

    @Override
    public ResponseEntity<InstallmentDto> createInstallment(InstallmentDto installmentDto, Long projectId,
            Long stageId) {
        var createdInstallmentDto = this.installmentApplicationService.createInstallment(projectId, stageId,
                installmentDto);
        var headers = new HttpHeadersBuilder().withLocation("project/{projectId}/installments/{installmentId}",
                projectId, installmentDto.getId()).build();
        return new ResponseEntity<>(createdInstallmentDto, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<InstallmentDto> updateInstallment(InstallmentDto installmentDto, Long projectId,
            Long installmentId) {
        return new ResponseEntity<>(
                this.installmentApplicationService.updateInstallment(projectId, installmentId, installmentDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteInstallment(Long projectId, Long installmentId) {
        this.installmentApplicationService.removeInstallment(projectId, installmentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<InstallmentDto> payInstallment(InstallmentDto installmentDto, Long projectId,
            Long installmentId) {
        return new ResponseEntity<>(
                this.installmentApplicationService.payInstallment(projectId, installmentId, installmentDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<InstallmentDto> getInstallment(Long projectId,
            @PathVariable("installmentId") Long installmentId) {
        return new ResponseEntity<>(this.installmentApplicationService.getInstallment(projectId, installmentId),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<InstallmentDto>> getInstallmentsForProject(Long projectId) {
        return new ResponseEntity<>(this.installmentApplicationService.getProjectInstallments(projectId),
                HttpStatus.OK);
    }

}
