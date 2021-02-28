package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.model.CooperatorJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CooperatorJobDtoMapper {
    CooperatorJobDtoMapper INSTANCE = Mappers.getMapper(CooperatorJobDtoMapper.class);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "value", target = "value")
    @Mapping(source = "contractorId", target = "cooperatorId")
    CooperatorJob contractorJobCreateDtoToCooperatorJob(ContractorJobDto contractorJobDto);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "value", target = "value")
    @Mapping(source = "cooperatorId", target = "contractorId")
    @Mapping(source = "note", target = "note")
    ContractorJobDto cooperatorJobToContractorJobDto(CooperatorJob cooperatorJob);
}
