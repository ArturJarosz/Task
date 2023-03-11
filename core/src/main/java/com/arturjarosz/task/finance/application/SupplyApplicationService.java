package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.finance.application.dto.SupplyDto;

import java.util.List;

public interface SupplyApplicationService {

    /**
     * Creates Supply for Project with given projectId. If Project does not exist or data provided in supplyDto are not
     * correct, then new exception will be thrown.
     */
    SupplyDto createSupply(Long projectId, SupplyDto supplyDto);

    /**
     * Update Supply with supplyId on Project with projectId according to data in supplyDto.
     * If Project or Supply on Project do not exist or data provided in supplyDto are not correct
     * new exception will be thrown.
     */
    SupplyDto updateSupply(Long projectId, Long supplyId, SupplyDto supplyDto);

    /**
     * Return Supply with supplyId on Project with projectId. If Project or Supply do not exist or
     * Supply does not belong to that Project, new exception will be thrown.
     */
    SupplyDto getSupply(Long projectId, Long supplyId);

    /**
     * Remove Supply with supplyId on Project with projectId. If Project or Supply do not exit,
     * new exception will be thrown.
     */
    void deleteSupply(Long projectId, Long supplyId);

    /**
     * Returns a list of all Supplies for Project with given projectId as a List of SupplyDto. If Project does not exist,
     * then exception with be exception will be thrown. If Project does not have any supplies, then the list will be
     * empty.
     */
    List<SupplyDto> getSuppliesForProject(Long projectId);
}
