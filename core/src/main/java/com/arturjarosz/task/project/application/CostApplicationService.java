package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.CostDto;

import java.util.List;

public interface CostApplicationService {
    /**
     * Creates Cost from CostDto for project with given projectId and return its Id.
     *
     * @param projectId id of Project
     * @param costDto   data of cost
     * @return CreatedEntityDto with id of created Cost
     */
    CostDto createCost(Long projectId, CostDto costDto);

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

    /**
     * Removes Cost with costId from given Project of projectId. If cost or project does not exist, then
     * exception is thrown.
     *
     * @param projectId
     * @param costId
     */
    void deleteCost(Long projectId, Long costId);

    /**
     * Update Cost of given costId on Project with given projectId according to data in CostDto.
     * If Project or Cost does not exist new exception is thrown.
     *
     * @param projectId
     * @param costId
     * @param costDto
     */
    CostDto updateCost(Long projectId, Long costId, CostDto costDto);
}
