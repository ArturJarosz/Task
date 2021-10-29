package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.model.CooperatorJob;
import com.arturjarosz.task.project.model.CooperatorJobType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CooperatorJobDtoMapper {
    CooperatorJobDtoMapper INSTANCE = Mappers.getMapper(CooperatorJobDtoMapper.class);

    default CooperatorJob contractorJobCreateDtoToCooperatorJob(ContractorJobDto contractorJobDto) {
        return new CooperatorJob(contractorJobDto.getName(), contractorJobDto.getContractorId(),
                CooperatorJobType.CONTRACTOR_JOB, contractorJobDto.getValue(), contractorJobDto.getHasInvoice(),
                contractorJobDto.getPayable());
    }

    ContractorJobDto cooperatorJobToContractorJobDto(CooperatorJob cooperatorJob);

}
