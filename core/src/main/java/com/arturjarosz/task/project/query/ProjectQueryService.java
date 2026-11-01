package com.arturjarosz.task.project.query;

import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.dto.StageDto;
import com.arturjarosz.task.dto.TaskDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.project.ProjectStatus;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Count all and active projects of each Architect from architectIds. Architects without projects get
     * ProjectCounts.NONE.
     */
    Map<Long, ProjectCounts> getProjectCountsByArchitectIds(Collection<Long> architectIds,
            Set<ProjectStatus> activeStatuses);

    /**
     * Count all and active projects of each Client from clientIds. Clients without projects get ProjectCounts.NONE.
     */
    Map<Long, ProjectCounts> getProjectCountsByClientIds(Collection<Long> clientIds, Set<ProjectStatus> activeStatuses);

    /**
     * Count projects of each Project type from projectTypeIds. Types without projects get ProjectCounts.NONE.
     */
    Map<Long, ProjectCounts> getProjectCountsByProjectTypeIds(Collection<Long> projectTypeIds);

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
