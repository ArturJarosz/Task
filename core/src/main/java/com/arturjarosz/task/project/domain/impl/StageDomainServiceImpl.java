package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.dto.StageDto;
import com.arturjarosz.task.project.application.mapper.StageMapper;
import com.arturjarosz.task.project.domain.StageDomainService;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.StageType;
import com.arturjarosz.task.project.status.stage.StageStatusTransitionService;
import com.arturjarosz.task.project.status.stage.StageWorkflow;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@DomainService
public class StageDomainServiceImpl implements StageDomainService {
    private final StageWorkflow stageWorkflow;
    private final StageStatusTransitionService stageStatusTransitionService;
    private final StageMapper stageMapper;

    @Override
    public Stage createStage(Project project, StageDto stageDto) {
        var stage = this.stageMapper.mapFromDto(stageDto, this.stageWorkflow);
        project.addStage(stage);
        this.stageStatusTransitionService.createStage(project, stage.getId());
        return stage;
    }

    @Override
    public Stage updateStage(Project project, Long stageId, StageDto stageDto) {
        return project.updateStage(stageId, stageDto.getName(), stageDto.getNote(),
                StageType.valueOf(stageDto.getType().name()), stageDto.getDeadline());
    }

    @Override
    public void rejectStage(Project project, Long stageId) {
        this.stageStatusTransitionService.reject(project, stageId);
    }

    @Override
    public void reopenStage(Project project, Long stageId) {
        this.stageStatusTransitionService.reopen(project, stageId);
    }
}
