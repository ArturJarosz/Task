package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.project.application.SupervisionApplicationService;
import com.arturjarosz.task.project.application.dto.SupervisionDto;
import com.arturjarosz.task.sharedkernel.utils.HttpHeadersBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("projects")
public class SupervisionRestController {

    private final SupervisionApplicationService supervisionApplicationService;

    public SupervisionRestController(SupervisionApplicationService supervisionApplicationService) {
        this.supervisionApplicationService = supervisionApplicationService;
    }

    @PostMapping("{projectId}/supervisions")
    public ResponseEntity<SupervisionDto> createSupervision(@PathVariable("projectId") Long projectId,
                                                            @RequestBody SupervisionDto supervisionDto) {
        SupervisionDto createdSupervisionDto = this.supervisionApplicationService
                .createSupervision(projectId, supervisionDto);
        HttpHeaders httpHeaders = new HttpHeadersBuilder()
                .withLocation("/projects/{projectId}/supervisions/{supervisionId}", projectId,
                        createdSupervisionDto.getId())
                .build();
        return new ResponseEntity<>(createdSupervisionDto, httpHeaders, HttpStatus.OK);
    }

    @PutMapping("{project}/supervisions/{supervisionId}")
    public ResponseEntity<SupervisionDto> updateSupervision() {
        return null;
    }

    public ResponseEntity<Void> deleteSupervision() {
        return null;
    }

    public ResponseEntity<SupervisionDto> getSupervision() {
        return null;
    }
}
