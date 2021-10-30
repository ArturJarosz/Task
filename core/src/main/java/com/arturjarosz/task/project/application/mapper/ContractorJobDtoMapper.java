package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.model.ContractorJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ContractorJobDtoMapper {
    ContractorJobDtoMapper INSTANCE = Mappers.getMapper(ContractorJobDtoMapper.class);

    default ContractorJob contractorJobDtoToContractorJob(ContractorJobDto contractorJobDto) {
        return new ContractorJob(contractorJobDto.getName(), contractorJobDto.getContractorId(),
                contractorJobDto.getValue(), contractorJobDto.getHasInvoice(),
                contractorJobDto.getPayable());
    }

    @Mapping(source = "projectId", target = "projectId")
    ContractorJobDto contractorJobToContractorJobDto(ContractorJob contractorJob, Long projectId);

    ContractorJobDto contractorJobToContractorJobDto(ContractorJob contractorJob);
}
