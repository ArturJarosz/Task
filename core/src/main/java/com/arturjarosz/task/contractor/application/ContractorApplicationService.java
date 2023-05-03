package com.arturjarosz.task.contractor.application;

import com.arturjarosz.task.contractor.application.dto.ContractorDto;

import java.util.List;

public interface ContractorApplicationService {
    /**
     * Create new Contractor based on data form contractorDto. If data provided in contractorDto are not correct
     * an exception will be thrown.
     */
    ContractorDto createContractor(ContractorDto contractorDto);

    /**
     * Update Contractor of given contractorId with data from contractorDto. If contractor does not exist or
     * data provided in dto are not correct an exception will be thrown.
     */
    void updateContractor(Long contractorId, ContractorDto contractorDto);

    /**
     * Remove Contractor with given contractorId. If Contractor does not exist, or has ContractorJobs, new exception
     * is thrown.
     */
    void deleteContractor(Long contractorId);

    /**
     * Get Contractor data in ContractorDto of given contractorId. If Contractor does not exist,
     * new exception is thrown.
     */
    ContractorDto getContractor(Long contractorId);

    /**
     * Get list of ContractorDto with basic Contractor data.
     */
    List<ContractorDto> getBasicContractors();

}
