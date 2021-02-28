package com.arturjarosz.task.project.query;

import com.arturjarosz.task.project.model.CooperatorJob;
import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.model.Stage;

public interface ProjectQueryService {

    /**
     * Loads Cost by given costId.
     *
     * @param costId
     * @return
     */
    Cost getCostById(Long costId);

    /**
     * Loads Stage by given stageId.
     *
     * @param stageId
     * @return
     */
    Stage getStageById(Long stageId);

    /**
     * Loads CooperatorJob by given cooperatorJobId.
     *
     * @param cooperatorJobId
     * @return
     */
    CooperatorJob getCooperatorJobByIdForProject(Long cooperatorJobId);
}
