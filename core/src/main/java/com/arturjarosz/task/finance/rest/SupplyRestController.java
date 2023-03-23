package com.arturjarosz.task.finance.rest;

import com.arturjarosz.task.finance.application.SupplyApplicationService;
import com.arturjarosz.task.finance.application.dto.SupplyDto;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SupplyRestController {

    private final SupplyApplicationService supplyApplicationService;

    @Autowired
    public SupplyRestController(SupplyApplicationService supplyApplicationService) {
        this.supplyApplicationService = supplyApplicationService;
    }

    @PostMapping("{projectId}/supplies")
    public ResponseEntity<SupplyDto> createSupply(@PathVariable("projectId") Long projectId,
            @RequestBody SupplyDto supplyDto) {
        SupplyDto createdSupplyDto = this.supplyApplicationService.createSupply(projectId, supplyDto);
        HttpHeaders headers = new HttpHeadersBuilder().withLocation("projects/{projectId}/supplies/{supplyId}",
                projectId, supplyDto.getId()).build();
        return new ResponseEntity<>(createdSupplyDto, headers, HttpStatus.CREATED);
    }

    @PutMapping("{projectId}/supplies/{supplyId}")
    public ResponseEntity<SupplyDto> updateSupply(@PathVariable("projectId") Long projectId,
            @PathVariable("supplyId") Long supplyId, @RequestBody SupplyDto supplyDto) {
        return new ResponseEntity<>(this.supplyApplicationService.updateSupply(projectId, supplyId, supplyDto),
                HttpStatus.OK);
    }

    @GetMapping("{projectId}/supplies/{supplyId}")
    public ResponseEntity<SupplyDto> getSupply(@PathVariable("projectId") Long projectId,
            @PathVariable("supplyId") Long supplyId) {
        return new ResponseEntity<>(this.supplyApplicationService.getSupply(projectId, supplyId), HttpStatus.OK);
    }

    @DeleteMapping("{projectId}/supplies/{supplyId}")
    public ResponseEntity<Void> deleteSupply(@PathVariable("projectId") Long projectId,
            @PathVariable("supplyId") Long supplyId) {
        this.supplyApplicationService.deleteSupply(projectId, supplyId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("{projectId}/supplies")
    public ResponseEntity<List<SupplyDto>> getSuppliesForProject(@PathVariable("projectId") Long projectId) {
        return new ResponseEntity<>(this.supplyApplicationService.getSuppliesForProject(projectId), HttpStatus.OK);
    }
}
