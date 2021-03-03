package com.arturjarosz.task.project.query;

import com.arturjarosz.task.project.model.CooperatorJob;
import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;

import java.util.List;

public interface ProjectQueryService {

    /**
     * Load Cost by given costId.
     *
     * @param costId
     * @return
     */
    Cost getCostById(Long costId);

    /**
     * Load Stage by given stageId.
     *
     * @param stageId
     * @return
     */
    Stage getStageById(Long stageId);

    /**
     * Load CooperatorJob by given cooperatorJobId.
     *
     * @param cooperatorJobId
     * @return
     */
    CooperatorJob getCooperatorJobByIdForProject(Long cooperatorJobId);

    /**
     * Load list of Project for given Client with clientId.
     *
     * @param clientId
     * @return
     */
    List<Project> getProjectForClientId(Long clientId);
}
