package com.arturjarosz.task.project.query;

import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.model.CooperatorJob;
import com.arturjarosz.task.project.model.CooperatorJobType;
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
    //TODO TA-191
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
    List<Project> getProjectsForClientId(Long clientId);

    /**
     * Load list of Project for given Architect with architectId.
     *
     * @param architectId
     * @return
     */
    List<Project> getProjectsForArchitect(Long architectId);

    /**
     * Retrieve Task as TaskDto of given TaskId.
     *
     * @return
     */
    TaskDto getTaskByTaskId(Long taskId);

    /**
     * Return List of Stages as StageDto for Project of given projectId.
     *
     * @param projectId
     * @return
     */
    List<StageDto> getStagesForProjectById(Long projectId);
    //TODO TA-191
    /**
     * Checks whether CooperatorJob with cooperatorJobId of given CooperatorJobType exists for Project with projectId.
     *
     * @param projectId
     * @param cooperatorJobId
     * @param cooperatorJobType
     * @return
     */
    CooperatorJob getCooperatorJobOfTypeExistsOnProject(Long projectId, Long cooperatorJobId,
                                                        CooperatorJobType cooperatorJobType);
}
