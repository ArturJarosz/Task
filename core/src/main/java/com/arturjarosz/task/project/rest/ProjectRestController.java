package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.dto.ProjectCreateDto;
import com.arturjarosz.task.dto.ProjectDto;
import com.arturjarosz.task.project.application.ProjectApplicationService;
import com.arturjarosz.task.rest.ProjectApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProjectRestController implements ProjectApi {

    @NonNull
    private final ProjectApplicationService projectApplicationService;

    @Override
    public ResponseEntity<ProjectDto> createProject(ProjectCreateDto projectCreateDto) {
        var createdProjectDto = this.projectApplicationService.createProject(projectCreateDto);
        var header = new HttpHeadersBuilder()
                .withLocation("/projects/{projectId}", createdProjectDto.getId())
                .build();
        return new ResponseEntity<>(createdProjectDto, header, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ProjectDto> getProject(Long projectId) {
        return new ResponseEntity<>(this.projectApplicationService.getProject(projectId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProjectDto> updateProject(ProjectDto projectDto, Long projectId) {
        return new ResponseEntity<>(this.projectApplicationService.updateProject(projectId, projectDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteProject(Long projectId) {
        this.projectApplicationService.removeProject(projectId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProjectDto> finishProject(ProjectDto projectDto, Long projectId) {
        return new ResponseEntity<>(this.projectApplicationService.finishProject(projectId, projectDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ProjectDto>> getProjects() {
        return new ResponseEntity<>(this.projectApplicationService.getProjects(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProjectDto> rejectProject(Long projectId) {
        return new ResponseEntity<>(this.projectApplicationService.rejectProject(projectId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProjectDto> reopenProject(Long projectId) {
        return new ResponseEntity<>(this.projectApplicationService.reopenProject(projectId), HttpStatus.OK);
    }
}
