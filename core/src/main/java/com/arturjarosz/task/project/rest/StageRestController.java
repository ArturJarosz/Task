package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.dto.StageDto;
import com.arturjarosz.task.project.application.StageApplicationService;
import com.arturjarosz.task.rest.StageApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StageRestController implements StageApi {

    @NonNull
    private final StageApplicationService stageApplicationService;

    @Override
    public ResponseEntity<StageDto> createStage(StageDto stageDto, Long projectId) {
        var createdStageDto = this.stageApplicationService.createStage(projectId, stageDto);
        var headers = new HttpHeadersBuilder()
                .withLocation("projects/{projectId}/stages/{stageId}", projectId, createdStageDto.getId())
                .build();
        return new ResponseEntity<>(createdStageDto, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteStage(Long projectId, Long stageId) {
        this.stageApplicationService.removeStage(projectId, stageId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StageDto> updateStage(StageDto stageDto, Long projectId, Long stageId) {
        return new ResponseEntity<>(this.stageApplicationService.updateStage(projectId, stageId, stageDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StageDto> getStage(Long projectId, Long stageId) {
        return new ResponseEntity<>(this.stageApplicationService.getStage(projectId, stageId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<StageDto>> getStagesForProject(Long projectId) {
        return new ResponseEntity<>(this.stageApplicationService.getStageListForProject(projectId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StageDto> rejectStage(Long projectId, Long stageId) {
        return new ResponseEntity<>(this.stageApplicationService.rejectStage(projectId, stageId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StageDto> reopenStage(Long projectId, Long stageId) {
        return new ResponseEntity<>(this.stageApplicationService.reopenStage(projectId, stageId), HttpStatus.OK);
    }
}
