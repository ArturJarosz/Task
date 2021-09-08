package com.arturjarosz.task.stage.domain;

import com.arturjarosz.task.stage.application.dto.StageDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.stage.model.Stage;

public interface StageDomainService {

    /**
     * Create a stage and add it to the project.
     *
     * @param project
     * @param stageDto
     * @return
     */
    Stage createStage(Project project, StageDto stageDto);

    /**
     * Update stage of given stageId with information provided in StageDto.
     *
     * @param project
     * @param stageId
     * @param stageDto
     * @return
     */
    Stage updateStage(Project project, Long stageId, StageDto stageDto);

    /**
     * Mark stage as rejected.
     *
     * @param project
     * @param stageId
     */
    void rejectStage(Project project, Long stageId);

    /**
     * Reopen rejected stage.
     *
     * @param project
     * @param stageId
     */
    void reopenStage(Project project, Long stageId);
}
