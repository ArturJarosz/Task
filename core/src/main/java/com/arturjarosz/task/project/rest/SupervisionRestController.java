package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.project.application.SupervisionApplicationService;
import com.arturjarosz.task.project.application.dto.SupervisionDto;
import com.arturjarosz.task.project.application.dto.SupervisionVisitDto;
import com.arturjarosz.task.sharedkernel.utils.HttpHeadersBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PutMapping("{project}/supervisions")
    public ResponseEntity<SupervisionDto> updateSupervision() {
        return null;
    }

    public ResponseEntity<Void> deleteSupervision() {
        return null;
    }

    public ResponseEntity<SupervisionDto> getSupervision() {
        return null;
    }

    @PostMapping("{projectId}/supervisions/supervisionVisits")
    public ResponseEntity<SupervisionVisitDto> addSupervisionVisit(@PathVariable("projectId") Long projectId,
                                                                   @RequestBody
                                                                           SupervisionVisitDto supervisionVisitDto) {
        SupervisionVisitDto createdSupervisionVisitDto = this.supervisionApplicationService
                .createSupervisionVisit(projectId, supervisionVisitDto);
        return new ResponseEntity<>(createdSupervisionVisitDto, HttpStatus.OK);
    }

    @GetMapping("{projectId}/supervisions/supervisionVisits/{supervisionVisitId}")
    public ResponseEntity<SupervisionVisitDto> getSupervisionVisit(@PathVariable("projectId") Long projectId,
                                                                   @PathVariable("supervisionVisitId") Long supervisionVisitId) {
        SupervisionVisitDto supervisionVisit = this.supervisionApplicationService.getSupervisionVisit(projectId,
                supervisionVisitId);
        HttpHeaders httpHeaders = new HttpHeadersBuilder()
                .withLocation("/projects/{projectId}/supervisions/{supervisionId}", projectId,
                        supervisionVisit.getId()).build();
        return new ResponseEntity<>(supervisionVisit, httpHeaders, HttpStatus.OK);
    }
}
