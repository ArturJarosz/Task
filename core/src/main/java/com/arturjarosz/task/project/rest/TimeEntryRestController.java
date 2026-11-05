package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.dto.TimeEntryDto;
import com.arturjarosz.task.project.application.TimeEntryApplicationService;
import com.arturjarosz.task.rest.TimeEntryApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class TimeEntryRestController implements TimeEntryApi {

    @NonNull
    private final TimeEntryApplicationService timeEntryApplicationService;

    @Override
    public ResponseEntity<List<TimeEntryDto>> getTimeEntries(Long projectId, Long stageId, Long taskId) {
        return new ResponseEntity<>(this.timeEntryApplicationService.getTimeEntries(projectId, stageId, taskId),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TimeEntryDto> createTimeEntry(TimeEntryDto timeEntryDto, Long projectId, Long stageId,
            Long taskId) {
        var createdTimeEntryDto = this.timeEntryApplicationService.createTimeEntry(projectId, stageId, taskId,
                timeEntryDto);
        var headers = new HttpHeadersBuilder().withLocation(
                "/projects/{projectId}/stages/{stageId}/tasks/{taskId}/time-entries/{timeEntryId}", projectId,
                stageId, taskId, createdTimeEntryDto.getId()).build();
        return new ResponseEntity<>(createdTimeEntryDto, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<TimeEntryDto> updateTimeEntry(TimeEntryDto timeEntryDto, Long projectId, Long stageId,
            Long taskId, Long timeEntryId) {
        return new ResponseEntity<>(
                this.timeEntryApplicationService.updateTimeEntry(projectId, stageId, taskId, timeEntryId,
                        timeEntryDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteTimeEntry(Long projectId, Long stageId, Long taskId, Long timeEntryId) {
        this.timeEntryApplicationService.deleteTimeEntry(projectId, stageId, taskId, timeEntryId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
