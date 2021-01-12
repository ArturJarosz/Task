package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.project.application.ProjectCostApplicationService;
import com.arturjarosz.task.project.application.dto.CostDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("projects")
public class ProjectCostRestController {

    private final ProjectCostApplicationService projectCostApplicationService;

    public ProjectCostRestController(
            ProjectCostApplicationService projectCostApplicationService) {
        this.projectCostApplicationService = projectCostApplicationService;
    }

    @PostMapping("{projectId}/costs")
    public ResponseEntity<CreatedEntityDto> createCost(@PathVariable("projectId") Long projectId,
                                                       @RequestBody CostDto costDto) {
        return new ResponseEntity<>(this.projectCostApplicationService.createCost(projectId, costDto),
                HttpStatus.CREATED);
    }

    @GetMapping("costs/{costId}")
    public ResponseEntity<CostDto> getCost(@PathVariable("costId") Long costId) {
        return new ResponseEntity<CostDto>(this.projectCostApplicationService.getCost(costId), HttpStatus.OK);
    }

    @GetMapping("{projectId}/costs")
    public ResponseEntity<List<CostDto>> getCostsForProject(@PathVariable("projectId") Long projectId) {
        return new ResponseEntity<>(this.projectCostApplicationService.getCosts(projectId), HttpStatus.OK);
    }

}
