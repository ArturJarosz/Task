package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.CostDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;

public interface ProjectCostApplicationService {
    /**
     * Creates Cost from CostDto for project with given projectId and return its Id.
     *
     * @param projectId id of Project
     * @param costDto   data of cost
     * @return CreatedEntityDto with id of created Cost
     */
    CreatedEntityDto createCost(Long projectId, CostDto costDto);

    /**
     * Return Cost by given costId.
     *
     * @param costId
     * @return
     */
    CostDto getCost(Long costId);

    /**
     * Return CostDto list for given projectId.
     * If project does not exist, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     *
     * @param projectId
     * @return
     */
    List<CostDto> getCosts(Long projectId);
}
