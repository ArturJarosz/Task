package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.project.application.CostApplicationService;
import com.arturjarosz.task.project.application.dto.CostDto;
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
public class CostRestController {

    private final CostApplicationService costApplicationService;

    public CostRestController(
            CostApplicationService costApplicationService) {
        this.costApplicationService = costApplicationService;
    }

    @PostMapping("{projectId}/costs")
    public ResponseEntity<CostDto> createCost(@PathVariable("projectId") Long projectId,
                                              @RequestBody CostDto costDto) {
        CostDto createdCostDto = this.costApplicationService.createCost(projectId, costDto);
        HttpHeaders headers = new HttpHeadersBuilder()
                .withLocation("/projects/{projectId}/costs/{costId}", projectId, createdCostDto.getId())
                .build();
        return new ResponseEntity<>(createdCostDto, headers, HttpStatus.CREATED);
    }

    @GetMapping("costs/{costId}")
    public ResponseEntity<CostDto> getCost(@PathVariable("costId") Long costId) {
        return new ResponseEntity<>(this.costApplicationService.getCost(costId), HttpStatus.OK);
    }

    @GetMapping("{projectId}/costs")
    public ResponseEntity<List<CostDto>> getCostsForProject(@PathVariable("projectId") Long projectId) {
        return new ResponseEntity<>(this.costApplicationService.getCosts(projectId), HttpStatus.OK);
    }

    @PutMapping("{projectId}/costs/{costId}")
    public ResponseEntity<CostDto> updateCost(@PathVariable("projectId") Long projectId,
                                              @PathVariable("costId") Long costId, @RequestBody CostDto costDto) {
        return new ResponseEntity<>(this.costApplicationService.updateCost(projectId, costId, costDto), HttpStatus.OK);
    }

    @DeleteMapping("{projectId}/costs/{costId}")
    public ResponseEntity<Void> deleteCost(@PathVariable("projectId") Long projectId,
                                           @PathVariable("costId") Long costId) {
        this.costApplicationService.deleteCost(projectId, costId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
