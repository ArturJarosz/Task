package com.arturjarosz.task.project.query;

import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.project.application.dto.SupplyDto;
import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.domain.dto.ProjectStatusData;
import com.arturjarosz.task.project.domain.dto.StageStatusData;
import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;

import java.util.List;

public interface ProjectQueryService {

    /**
     * Load Cost by given costId.
     */
    Cost getCostById(Long costId);

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
     * Returns Supply with supplyId for Project with projectId.
     */
    SupplyDto getSupplyForProject(long supplyId, long projectId);

    /**
     * Returns ContractorJob with contractorJobId for Project with projectId.
     */
    ContractorJobDto getContractorJobForProject(long contractorJobId, long projectId);

    /**
     * Get contract status for Project with given projectId.
     */
    ContractStatus getContractStatusForProject(long projectId);

    ProjectStatusData getProjectStatusData(long projectId);

    StageStatusData getStageStatusData(long stageId);

    /**
     * Returns all Supplies as a List of SupplyDto for Project with given projectId.
     */
    List<SupplyDto> getSuppliesForProject(Long projectId);
}
