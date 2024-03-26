package com.arturjarosz.task.project.query;

import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.dto.StageDto;
import com.arturjarosz.task.dto.TaskDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;

import java.util.List;

public interface ProjectQueryService {

    /**
     * Load Stage by given stageId.
     */
    Stage getStageById(long stageId);

    /**
     * Load list of Project for given Client with clientId.
     */
    List<Project> getProjectsForClientId(long clientId);

    /**
     * Load list of Project for given Architect with architectId.
     */
    List<Project> getProjectsForArchitect(long architectId);

    /**
     * Retrieve Task as TaskDto of given TaskId.
     */
    TaskDto getTaskByTaskId(long taskId);

    /**
     * Return List of Stages as StageDto for Project of given projectId.
     */
    List<StageDto> getStagesForProjectById(long projectId);

    /**
     * Get contract status for Project with given projectId.
     */
    ContractStatus getContractStatusForProject(long projectId);

    Boolean doesProjectExistByProjectId(long projectId);

    Long getInstallmentIdForStage(long stageId);
}
