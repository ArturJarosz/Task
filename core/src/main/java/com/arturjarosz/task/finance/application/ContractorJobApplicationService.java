package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.finance.application.dto.ContractorJobDto;

public interface ContractorJobApplicationService {
    /**
     * Create ContractorJob for Project with given projectId. If Project does not exist or data provided in
     * contractorJobDto are not correct, then new exception will be thrown.
     */
    ContractorJobDto createContractorJob(Long projectId, ContractorJobDto contractorJobDto);

    /**
     * Remove ContractorJob with contractorJobId on Project with projectId. If Project or ContractorJob do not exit,
     * new exception will be thrown.
     */
    void deleteContractorJob(Long projectId, Long contractorJobId);

    /**
     * Update ContractorJob with contractorJobId on Project with projectId according to data in contractorJobDto.
     * If Project or ContractorJob on Project do not exist or data provided in contractorJobDto are not correct
     * new exception will be thrown.
     */
    ContractorJobDto updateContractorJob(Long projectId, Long contractorJobId, ContractorJobDto contractorJobDto);

    /**
     * Return ContractorJob with contractorJobId on Project with projectId. If Project or ContractorJob do not exist or
     * ContractorJob does not belong to that Project, new exception will be thrown.
     */
    ContractorJobDto getContractorJob(Long projectId, Long contractorJobId);
}
