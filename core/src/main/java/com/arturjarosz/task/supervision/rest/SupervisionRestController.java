package com.arturjarosz.task.supervision.rest;

import com.arturjarosz.task.supervision.application.SupervisionApplicationService;
import com.arturjarosz.task.supervision.application.dto.SupervisionDto;
import com.arturjarosz.task.supervision.application.dto.SupervisionVisitDto;
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

@RestController
@RequestMapping("supervisions")
public class SupervisionRestController {

    private final SupervisionApplicationService supervisionApplicationService;

    public SupervisionRestController(SupervisionApplicationService supervisionApplicationService) {
        this.supervisionApplicationService = supervisionApplicationService;
    }

    @PostMapping()
    public ResponseEntity<SupervisionDto> createSupervision(@RequestBody SupervisionDto supervisionDto) {
        SupervisionDto createdSupervisionDto = this.supervisionApplicationService
                .createSupervision(supervisionDto);
        HttpHeaders httpHeaders = new HttpHeadersBuilder()
                .withLocation("supervisions/{supervisionId}", createdSupervisionDto.getId())
                .build();
        return new ResponseEntity<>(createdSupervisionDto, httpHeaders, HttpStatus.OK);
    }

    @PutMapping("{supervisionId}")
    public ResponseEntity<SupervisionDto> updateSupervision(@PathVariable("supervisionId") Long supervisionId,
                                                            @RequestBody SupervisionDto supervisionDto) {
        return new ResponseEntity<>(this.supervisionApplicationService.updateSupervision(supervisionId, supervisionDto),
                HttpStatus.OK);
    }

    @DeleteMapping("{supervisionId}")
    public ResponseEntity<Void> deleteSupervision(@PathVariable("supervisionId") Long supervisionId) {
        this.supervisionApplicationService.deleteSupervision(supervisionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("{supervisionId}")
    public ResponseEntity<SupervisionDto> getSupervision(@PathVariable("supervisionId") Long supervisionId) {
        return new ResponseEntity<>(this.supervisionApplicationService.getSupervision(supervisionId), HttpStatus.OK);
    }

    @PostMapping("{supervisionId}/supervisionVisits")
    public ResponseEntity<SupervisionVisitDto> addSupervisionVisit(@PathVariable("supervisionId") Long supervisionId,
                                                                   @RequestBody SupervisionVisitDto supervisionVisitDto) {
        SupervisionVisitDto createdSupervisionVisitDto = this.supervisionApplicationService
                .createSupervisionVisit(supervisionId, supervisionVisitDto);
        HttpHeaders httpHeaders = new HttpHeadersBuilder()
                .withLocation("supervisions/supervisionId/supervisionVisits{supervisionVisitId}", supervisionId,
                        createdSupervisionVisitDto.getId()).build();
        return new ResponseEntity<>(createdSupervisionVisitDto, httpHeaders, HttpStatus.OK);
    }

    @PutMapping("{supervisionId}/supervisionVisits/{supervisionVisitId}")
    public ResponseEntity<SupervisionVisitDto> updateSupervisionVisit(@PathVariable("supervisionId") Long supervisionId,
                                                                      @PathVariable("supervisionVisitId") Long supervisionVisitId,
                                                                      @RequestBody SupervisionVisitDto supervisionVisitDto) {
        return new ResponseEntity<>(
                this.supervisionApplicationService.updateSupervisionVisit(supervisionId, supervisionVisitId,
                        supervisionVisitDto),
                HttpStatus.OK);
    }

    @GetMapping("{supervisionId}/supervisionVisits/{supervisionVisitId}")
    public ResponseEntity<SupervisionVisitDto> getSupervisionVisit(@PathVariable("supervisionId") Long supervisionId,
                                                                   @PathVariable("supervisionVisitId") Long supervisionVisitId) {
        SupervisionVisitDto supervisionVisit = this.supervisionApplicationService.getSupervisionVisit(supervisionId,
                supervisionVisitId);
        return new ResponseEntity<>(supervisionVisit, HttpStatus.OK);
    }

    @DeleteMapping("{supervisionId}/supervisionVisits/{supervisionVisitId}")
    public ResponseEntity<Void> deleteSupervisionVisit(@PathVariable("supervisionId") Long supervisionId,
                                                       @PathVariable("supervisionVisitId") Long supervisionVisitId) {
        this.supervisionApplicationService.deleteSupervisionVisit(supervisionId, supervisionVisitId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
