package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.dto.ContractorJobDto;
import com.arturjarosz.task.finance.model.ContractorJob;
import org.mapstruct.Mapper;

@Mapper
public interface ContractorJobMapper {

    default ContractorJob mapFromDto(ContractorJobDto contractorJobDto) {
        return new ContractorJob(contractorJobDto.getName(), contractorJobDto.getContractorId(),
                contractorJobDto.getValue(), contractorJobDto.getHasInvoice(),
                contractorJobDto.getPayable());
    }

    ContractorJobDto mapToDto(ContractorJob contractorJob, Long projectId);

}
