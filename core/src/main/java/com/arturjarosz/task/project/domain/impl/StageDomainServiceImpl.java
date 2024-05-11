package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.dto.StageDto;
import com.arturjarosz.task.project.application.mapper.StageMapper;
import com.arturjarosz.task.project.domain.StageDomainService;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.StageType;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.stage.StageStatusTransitionService;
import com.arturjarosz.task.project.status.stage.StageWorkflow;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

@DomainService
public class StageDomainServiceImpl implements StageDomainService {
    private final StageWorkflow stageWorkflow;
    private final StageStatusTransitionService stageStatusTransitionService;
    private final StageMapper stageMapper;

    private Map<StageStatus, BiConsumer<Stage, StageDto>> statusToUpdater;

    @Autowired
    public StageDomainServiceImpl(StageWorkflow stageWorkflow,
            StageStatusTransitionService stageStatusTransitionService, StageMapper stageMapper) {
        this.stageWorkflow = stageWorkflow;
        this.stageStatusTransitionService = stageStatusTransitionService;
        this.stageMapper = stageMapper;
        this.prepareStageUpdater();
    }

    private void prepareStageUpdater() {
        this.statusToUpdater = new EnumMap<>(StageStatus.class);
        this.statusToUpdater.put(StageStatus.TO_DO,
                (stage, stageDto) -> stage.update(stageDto.getName(), stageDto.getNote(),
                        StageType.valueOf(stageDto.getType().getValue()), stageDto.getDeadline()));
        this.statusToUpdater.put(StageStatus.IN_PROGRESS, (stage, stageDto) -> {
            stage.update(stageDto.getName(), stageDto.getNote(), StageType.valueOf(stageDto.getType().getValue()),
                    stageDto.getDeadline());
            stage.setStartDate(stageDto.getStartDate());
            stage.setEndDate(null);
        });
        this.statusToUpdater.put(StageStatus.DONE, (stage, stageDto) -> {
            stage.update(stageDto.getName(), stageDto.getNote(), StageType.valueOf(stageDto.getType().getValue()),
                    stageDto.getDeadline());
            stage.setStartDate(stageDto.getStartDate());
            stage.setEndDate(stageDto.getEndDate());
        });
        this.statusToUpdater.put(StageStatus.REJECTED, (stage, stageDto) -> {
            stage.update(stageDto.getName(), stageDto.getNote(), StageType.valueOf(stageDto.getType().getValue()),
                    stageDto.getDeadline());
            stage.setStartDate(stageDto.getStartDate());
        });
    }

    @Override
    public Stage createStage(Project project, StageDto stageDto) {
        var stage = this.stageMapper.mapFromDto(stageDto, this.stageWorkflow);
        project.addStage(stage);
        this.stageStatusTransitionService.createStage(project, stage.getId());
        return stage;
    }

    @Override
    public Stage updateStage(Project project, Long stageId, StageDto stageDto) {
        var maybeStage = project.getStages().stream().filter(stage -> stage.getId().equals(stageId)).findFirst();
        if (maybeStage.isEmpty()) {
            throw new ResourceNotFoundException();
        }
        var stage = maybeStage.get();
        var stageUpdater = this.statusToUpdater.get(stage.getStatus());
        stageUpdater.accept(stage, stageDto);

        return stage;
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
