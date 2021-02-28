package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

public interface ContractorJobApplicationService {
    /**
     * Create ContractorJob for Project with given projectId. If Project does not exist or data provided in
     * contractorJobDto are not correct, then new exception will be thrown.
     *
     * @param projectId
     * @param contractorJobDto
     * @return
     */
    CreatedEntityDto createContractorJob(Long projectId, ContractorJobDto contractorJobDto);

    /**
     * Remove ContractorJob with contractorJobId on Project with projectId. If Project or ContractorJob do not exit,
     * new exception will be thrown.
     *
     * @param projectId
     * @param contractorJobId
     */
    void deleteContractorJob(Long projectId, Long contractorJobId);

    /**
     * Update ContractorJob with contractorJobId on Project with projectId according to data in contractorJobDto.
     * If Project or ContractorJob on Project do not exist or data provided in contractorJobDto are not correct
     * new exception will be thrown.
     *
     * @param projectId
     * @param contractorJobId
     * @param contractorJobDto
     */
    void updateContractorJob(Long projectId, Long contractorJobId, ContractorJobDto contractorJobDto);

    /**
     * Return ContractorJob with contractorJobId on Project with projectId. If Project or ContractorJob do not exist or
     * ContractorJob does not belonag to that Project, new exception will be thrown.
     *
     * @param projectId
     * @param contractorJobId
     * @return
     */
    ContractorJobDto getContractorJob(Long projectId, Long contractorJobId);
}
