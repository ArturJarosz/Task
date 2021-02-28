package com.arturjarosz.task.cooperator.application;

import com.arturjarosz.task.cooperator.application.dto.ContractorDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;

public interface ContractorApplicationService {
    /**
     * Create new Contractor based on data form contractorDto. If data provided in contractorDto are not correct
     * an exception will be thrown.
     *
     * @param contractorDto
     * @return
     */
    CreatedEntityDto createContractor(ContractorDto contractorDto);

    /**
     * Update Contractor of given contractorId with data from contractorDto. If contractor does not exists or
     * data provided in dto are not correct an exception will be thrown.
     *
     * @param contractorId
     * @param contractorDto
     */
    void updateContractor(Long contractorId, ContractorDto contractorDto);

    /**
     * Remove Contractor with given contractorId. If Contractor does not exist, or has ContractorJobs, new exception
     * is thrown.
     *
     * @param contractorId
     */
    void deleteContractor(Long contractorId);

    /**
     * Get Contractor data in ContractorDto of given contractorId. If Contractor does not exist,
     * new exception is thrown.
     *
     * @param contractorId
     * @return
     */
    ContractorDto getContractor(Long contractorId);

    /**
     * Get list of ContractorDto with basic Contractor data.
     *
     * @return
     */
    List<ContractorDto> getBasicContractors();

}
