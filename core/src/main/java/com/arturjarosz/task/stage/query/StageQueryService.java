package com.arturjarosz.task.stage.query;

import com.arturjarosz.task.stage.model.Stage;

public interface StageQueryService {
    /**
     * Load Stage by given stageId.
     *
     * @param stageId
     * @return
     */
    Stage getStageById(Long stageId);
}
