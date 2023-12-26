package com.arturjarosz.task.finance.rest;

import com.arturjarosz.task.dto.CostDto;
import com.arturjarosz.task.finance.application.CostApplicationService;
import com.arturjarosz.task.rest.CostApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class CostRestController implements CostApi {

    @NonNull
    private final CostApplicationService costApplicationService;

    @Override
    public ResponseEntity<CostDto> createCost(CostDto costDto, Long projectId) {
        var createdCostDto = this.costApplicationService.createCost(projectId, costDto);
        var headers = new HttpHeadersBuilder()
                .withLocation("/projects/{projectId}/costs/{costId}", projectId, createdCostDto.getId())
                .build();
        return new ResponseEntity<>(createdCostDto, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CostDto> getCost(Long costId) {
        return new ResponseEntity<>(this.costApplicationService.getCost(costId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CostDto>> getCostsForProject(Long projectId) {
        return new ResponseEntity<>(this.costApplicationService.getCosts(projectId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CostDto> updateCost(CostDto costDto, Long projectId, Long costId) {
        return new ResponseEntity<>(this.costApplicationService.updateCost(projectId, costId, costDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteCost(Long projectId, Long costId) {
        this.costApplicationService.deleteCost(projectId, costId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
