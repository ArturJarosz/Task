package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.SupplyDto;

public interface SupplyApplicationService {

    /**
     * Creates Supply for Project with given projectId. If Project does not exist or data provided in supplyDto are not
     * correct, then new exception will be thrown.
     *
     * @param projectId
     * @param supplyDto
     * @return
     */
    SupplyDto createSupply(Long projectId, SupplyDto supplyDto);

    /**
     * Update Supply with supplyId on Project with projectId according to data in supplyDto.
     * If Project or Supply on Project do not exist or data provided in supplyDto are not correct
     * new exception will be thrown.
     *
     * @param projectId
     * @param supplyId
     * @param supplyDto
     * @return
     */
    SupplyDto updateSupply(Long projectId, Long supplyId, SupplyDto supplyDto);

    /**
     * Return Supply with supplyId on Project with projectId. If Project or Supply do not exist or
     * Supply does not belong to that Project, new exception will be thrown.
     *
     * @param projectId
     * @param supplyId
     * @return
     */
    SupplyDto getSupply(Long projectId, Long supplyId);

    /**
     * Remove Supply with supplyId on Project with projectId. If Project or Supply do not exit,
     * new exception will be thrown.
     *
     * @param projectId
     * @param supplyId
     */
    void deleteSupply(Long projectId, Long supplyId);
}
