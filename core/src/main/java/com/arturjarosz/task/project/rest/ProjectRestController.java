package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.project.application.ProjectApplicationService;
import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
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
public class ProjectRestController {

    private final ProjectApplicationService projectApplicationService;

    public ProjectRestController(ProjectApplicationService projectApplicationService) {
        this.projectApplicationService = projectApplicationService;
    }

    @PostMapping("")
    public ResponseEntity<ProjectDto> createProject(@RequestBody ProjectCreateDto projectCreateDto) {
        ProjectDto createdProjectDto = this.projectApplicationService.createProject(projectCreateDto);
        HttpHeaders header = new HttpHeadersBuilder()
                .withLocation("projects/{projectId}", createdProjectDto.getId())
                .build();
        return new ResponseEntity<>(createdProjectDto, header, HttpStatus.CREATED);
    }

    @GetMapping("{projectId}")
    public ResponseEntity<ProjectDto> getProject(@PathVariable("projectId") Long projectId) {
        return new ResponseEntity<>(this.projectApplicationService.getProject(projectId), HttpStatus.OK);
    }

    @PutMapping("{projectId}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable("projectId") Long projectId,
                                                    @RequestBody ProjectDto projectDto) {
        this.projectApplicationService.updateProject(projectId, projectDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("{projectId}")
    public ResponseEntity<Void> removeProject(@PathVariable("projectId") Long projectId) {
        this.projectApplicationService.removeProject(projectId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("{projectId}/sign")
    public ResponseEntity<Void> signProjectContract(@PathVariable("projectId") Long projectId, @RequestBody
            ProjectContractDto projectContractDto) {
        this.projectApplicationService.signProjectContract(projectId, projectContractDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("{projectId}/finish")
    public ResponseEntity<Void> finishProject(@PathVariable("projectId") Long projectId, @RequestBody
            ProjectContractDto projectContractDto) {
        this.projectApplicationService.finishProject(projectId, projectContractDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<ProjectDto>> getProjects() {
        return new ResponseEntity<>(this.projectApplicationService.getProjects(), HttpStatus.OK);
    }

    @PostMapping("{projectId}/reject")
    public ResponseEntity<Void> rejectProject(@PathVariable("projectId") Long projectId) {
        this.projectApplicationService.rejectProject(projectId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("{projectId}/newOffer")
    public ResponseEntity<Void> makeNewOffer(@PathVariable("projectId") Long projectId) {
        this.projectApplicationService.makeNewOffer(projectId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
