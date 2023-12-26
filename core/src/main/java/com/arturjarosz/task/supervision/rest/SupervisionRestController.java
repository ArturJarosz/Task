package com.arturjarosz.task.supervision.rest;

import com.arturjarosz.task.dto.SupervisionDto;
import com.arturjarosz.task.dto.SupervisionVisitDto;
import com.arturjarosz.task.rest.SupervisionApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import com.arturjarosz.task.supervision.application.SupervisionApplicationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SupervisionRestController implements SupervisionApi {

    @NonNull
    private final SupervisionApplicationService supervisionApplicationService;

    @Override
    public ResponseEntity<SupervisionDto> createSupervision(SupervisionDto supervisionDto) {
        var createdSupervisionDto = this.supervisionApplicationService
                .createSupervision(supervisionDto);
        var httpHeaders = new HttpHeadersBuilder()
                .withLocation("supervisions/{supervisionId}", createdSupervisionDto.getId())
                .build();
        return new ResponseEntity<>(createdSupervisionDto, httpHeaders, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SupervisionDto> updateSupervision(SupervisionDto supervisionDto, Long supervisionId) {
        return new ResponseEntity<>(this.supervisionApplicationService.updateSupervision(supervisionId, supervisionDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteSupervision(Long supervisionId) {
        this.supervisionApplicationService.deleteSupervision(supervisionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SupervisionDto> getSupervision(Long supervisionId) {
        return new ResponseEntity<>(this.supervisionApplicationService.getSupervision(supervisionId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SupervisionVisitDto> createSupervisionVisit(SupervisionVisitDto supervisionVisitDto, Long supervisionId) {
        var createdSupervisionVisitDto = this.supervisionApplicationService
                .createSupervisionVisit(supervisionId, supervisionVisitDto);
        var httpHeaders = new HttpHeadersBuilder()
                .withLocation("supervisions/supervisionId/supervision-visits{supervisionVisitId}", supervisionId,
                        createdSupervisionVisitDto.getId()).build();
        return new ResponseEntity<>(createdSupervisionVisitDto, httpHeaders, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SupervisionVisitDto> updateSupervisionVisit(SupervisionVisitDto supervisionVisitDto, Long supervisionId, Long supervisionVisitId) {
        return new ResponseEntity<>(
                this.supervisionApplicationService.updateSupervisionVisit(supervisionId, supervisionVisitId,
                        supervisionVisitDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SupervisionVisitDto> getSupervisionVisit(Long supervisionId, Long supervisionVisitId) {
        var supervisionVisit = this.supervisionApplicationService.getSupervisionVisit(supervisionId,
                supervisionVisitId);
        return new ResponseEntity<>(supervisionVisit, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteSupervisionVisit(Long supervisionId, Long supervisionVisitId) {
        this.supervisionApplicationService.deleteSupervisionVisit(supervisionId, supervisionVisitId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
