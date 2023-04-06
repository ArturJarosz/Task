package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.project.application.dto.CostDto;

import java.util.List;

public interface CostApplicationService {
    /**
     * Creates Cost from CostDto for project with given projectId and return its projectId.
     *
     * @param projectId id of Project
     * @param costDto   data of cost
     * @return CreatedEntityDto with id of created Cost
     */
    CostDto createCost(Long projectId, CostDto costDto);

    /**
     * Return Cost by given costId.
     */
    CostDto getCost(Long costId);

    /**
     * Return CostDto list for given projectId.
     * If project does not exist, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    List<CostDto> getCosts(Long projectId);

    /**
     * Removes Cost with costId from given Project of projectId. If cost or project does not exist, then
     * exception is thrown.
     */
    void deleteCost(Long projectId, Long costId);

    /**
     * Update Cost of given costId on Project with given projectId according to data in CostDto.
     * If Project or Cost does not exist new exception is thrown.
     */
    CostDto updateCost(Long projectId, Long costId, CostDto costDto);
}
