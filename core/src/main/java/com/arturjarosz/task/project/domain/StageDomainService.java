package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.dto.StageDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;

public interface StageDomainService {

    /**
     * Create a stage and add it to the project.
     */
    Stage createStage(Project project, StageDto stageDto);

    /**
     * Update stage of given stageId with information provided in StageDto.
     */
    Stage updateStage(Project project, Long stageId, StageDto stageDto);

    /**
     * Mark stage as rejected.
     */
    void rejectStage(Project project, Long stageId);

    /**
     * Reopen rejected stage.
     */
    void reopenStage(Project project, Long stageId);
}
