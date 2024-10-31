package com.arturjarosz.task.finance.rest;

import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.dto.SupplyProjectDataDto;
import com.arturjarosz.task.finance.application.SupplyApplicationService;
import com.arturjarosz.task.rest.SupplyApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SupplyRestController implements SupplyApi {

    @NonNull
    private final SupplyApplicationService supplyApplicationService;

    @Override
    public ResponseEntity<SupplyDto> createSupply(SupplyDto supplyDto, Long projectId) {
        var createdSupplyDto = this.supplyApplicationService.createSupply(projectId, supplyDto);
        var headers = new HttpHeadersBuilder().withLocation("/projects/{projectId}/supplies/{supplyId}",
                projectId, createdSupplyDto.getId()).build();
        return new ResponseEntity<>(createdSupplyDto, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SupplyDto> updateSupply(SupplyDto supplyDto, Long projectId, Long supplyId) {
        return new ResponseEntity<>(this.supplyApplicationService.updateSupply(projectId, supplyId, supplyDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SupplyDto> getSupply(Long projectId, Long supplyId) {
        return new ResponseEntity<>(this.supplyApplicationService.getSupply(projectId, supplyId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteSupply(Long projectId, Long supplyId) {
        this.supplyApplicationService.deleteSupply(projectId, supplyId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SupplyProjectDataDto> getProjectSuppliesData(Long projectId) {
        return new ResponseEntity<>(this.supplyApplicationService.getProjectSuppliesData(projectId), HttpStatus.OK);
    }
}
