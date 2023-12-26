package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.dto.ContractorJobDto;
import com.arturjarosz.task.finance.model.ContractorJob;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ContractorJobDtoMapper {
    ContractorJobDtoMapper INSTANCE = Mappers.getMapper(ContractorJobDtoMapper.class);

    default ContractorJob contractorJobDtoToContractorJob(ContractorJobDto contractorJobDto) {
        return new ContractorJob(contractorJobDto.getName(), contractorJobDto.getContractorId(),
                contractorJobDto.getValue(), contractorJobDto.getHasInvoice(),
                contractorJobDto.getPayable());
    }

    ContractorJobDto contractorJobToContractorJobDto(ContractorJob contractorJob, Long projectId);

    ContractorJobDto contractorJobToContractorJobDto(ContractorJob contractorJob);
}
