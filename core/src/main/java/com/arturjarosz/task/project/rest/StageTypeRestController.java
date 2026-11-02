package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.dto.StageTypeDto;
import com.arturjarosz.task.project.application.StageTypeApplicationService;
import com.arturjarosz.task.rest.StageTypeApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class StageTypeRestController implements StageTypeApi {

    @NonNull
    private final StageTypeApplicationService stageTypeApplicationService;

    @Override
    public ResponseEntity<StageTypeDto> createStageType(StageTypeDto stageTypeDto) {
        var stageType = this.stageTypeApplicationService.create(stageTypeDto);
        var headers = new HttpHeadersBuilder().withLocation("/stage-types/{stageTypeId}", stageType.getId())
                .build();
        return new ResponseEntity<>(stageType, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<StageTypeDto> updateStageType(StageTypeDto stageTypeDto, Long stageTypeId) {
        return new ResponseEntity<>(this.stageTypeApplicationService.update(stageTypeId, stageTypeDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteStageType(Long stageTypeId) {
        this.stageTypeApplicationService.delete(stageTypeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StageTypeDto> getStageType(Long stageTypeId) {
        return new ResponseEntity<>(this.stageTypeApplicationService.get(stageTypeId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<StageTypeDto>> getStageTypes() {
        return new ResponseEntity<>(this.stageTypeApplicationService.getAll(), HttpStatus.OK);
    }
}
