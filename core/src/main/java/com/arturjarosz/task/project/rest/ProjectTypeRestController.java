package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.dto.ProjectTypeDto;
import com.arturjarosz.task.project.application.ProjectTypeApplicationService;
import com.arturjarosz.task.rest.ProjectTypeApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProjectTypeRestController implements ProjectTypeApi {

    @NonNull
    private final ProjectTypeApplicationService projectTypeApplicationService;

    @Override
    public ResponseEntity<ProjectTypeDto> createProjectType(ProjectTypeDto projectTypeDto) {
        var projectType = this.projectTypeApplicationService.createProjectType(projectTypeDto);
        var headers = new HttpHeadersBuilder().withLocation("/project-types/{projectTypeId}", projectType.getId())
                .build();
        return new ResponseEntity<>(projectType, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ProjectTypeDto> updateProjectType(ProjectTypeDto projectTypeDto, Long projectTypeId) {
        return new ResponseEntity<>(this.projectTypeApplicationService.updateProjectType(projectTypeId, projectTypeDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteProjectType(Long projectTypeId) {
        this.projectTypeApplicationService.deleteProjectType(projectTypeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProjectTypeDto> getProjectType(Long projectTypeId) {
        return new ResponseEntity<>(this.projectTypeApplicationService.getProjectType(projectTypeId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ProjectTypeDto>> getProjectTypes() {
        return new ResponseEntity<>(this.projectTypeApplicationService.getProjectTypes(), HttpStatus.OK);
    }
}
