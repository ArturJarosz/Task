package com.arturjarosz.task.project.query;

import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.domain.dto.ProjectStatusData;
import com.arturjarosz.task.project.domain.dto.StageStatusData;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;

import java.util.List;

public interface ProjectQueryService {

    /**
     * Load Stage by given stageId.
     */
    Stage getStageById(Long stageId);

    /**
     * Load list of Project for given Client with clientId.
     */
    List<Project> getProjectsForClientId(Long clientId);

    /**
     * Load list of Project for given Architect with architectId.
     */
    List<Project> getProjectsForArchitect(Long architectId);

    /**
     * Retrieve Task as TaskDto of given TaskId.
     */
    TaskDto getTaskByTaskId(Long taskId);

    /**
     * Return List of Stages as StageDto for Project of given projectId.
     */
    List<StageDto> getStagesForProjectById(Long projectId);

    /**
     * Get contract status for Project with given projectId.
     */
    ContractStatus getContractStatusForProject(long projectId);

    ProjectStatusData getProjectStatusData(long projectId);

    StageStatusData getStageStatusData(long stageId);

    Boolean doesProjectExistByProjectId(Long projectId);
}
