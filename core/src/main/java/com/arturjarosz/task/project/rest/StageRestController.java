package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.project.application.StageApplicationService;
import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
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
@RequestMapping("projects/{projectId}")
public class StageRestController {

    private final StageApplicationService stageApplicationService;

    public StageRestController(StageApplicationService stageApplicationService) {
        this.stageApplicationService = stageApplicationService;
    }

    @PostMapping("stages")
    public ResponseEntity<StageDto> createStage(@PathVariable("projectId") Long projectId,
                                                @RequestBody StageDto stageDto) {
        StageDto createdStageDto = this.stageApplicationService.createStage(projectId, stageDto);
        HttpHeaders headers = new HttpHeadersBuilder()
                .withLocation("projects/{projectId}/stages/{stageId}", projectId, createdStageDto.getId())
                .build();
        return new ResponseEntity<>(createdStageDto, headers, HttpStatus.CREATED);
    }

    @DeleteMapping("stages/{stageId}")
    public ResponseEntity<Void> removeStage(@PathVariable("projectId") Long projectId,
                                            @PathVariable("stageId") Long stageId) {
        this.stageApplicationService.removeStage(projectId, stageId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("stages/{stageId}")
    public ResponseEntity<StageDto> updateStage(@PathVariable("projectId") Long projectId,
                                                @PathVariable("stageId") Long stageId, @RequestBody StageDto stageDto) {

        return new ResponseEntity<>(this.stageApplicationService.updateStage(projectId, stageId, stageDto),
                HttpStatus.OK);
    }

    @GetMapping("stages/{stageId}")
    public ResponseEntity<StageDto> getStage(@PathVariable("projectId") Long projectId,
                                             @PathVariable("stageId") Long stageId) {
        return new ResponseEntity<>(this.stageApplicationService.getStage(projectId, stageId), HttpStatus.OK);
    }

    @GetMapping("stages")
    public ResponseEntity<List<StageDto>> getStages(@PathVariable("projectId") Long projectId) {
        return new ResponseEntity<>(this.stageApplicationService.getStageListForProject(projectId), HttpStatus.OK);
    }

    @PostMapping("stages/{stageId}/reject")
    public ResponseEntity<StageDto> rejectStage(@PathVariable("projectId") Long projectId,
                                                @PathVariable("stageId") Long stageId) {
        return new ResponseEntity<>(this.stageApplicationService.rejectStage(projectId, stageId), HttpStatus.OK);
    }

    @PostMapping("stages/{stageId}/reopen")
    public ResponseEntity<StageDto> reopenStage(@PathVariable("projectId") Long projectId,
                                                @PathVariable("stageId") Long stageId) {
        return new ResponseEntity<>(this.stageApplicationService.reopenStage(projectId, stageId), HttpStatus.OK);
    }
}
