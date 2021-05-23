package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;

public interface StageDomainService {

    Stage createStage(Project project, StageDto stageDto);

    Stage updateStage(Project project, Long stageId, StageDto stageDto);

    void rejectStage(Project project, Long stageId);

    void reopenStage(Project project, Long stageId);
}
