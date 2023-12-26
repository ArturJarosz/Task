package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.dto.CostDto;
import com.arturjarosz.task.finance.model.Cost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CostDtoMapper {
    CostDtoMapper INSTANCE = Mappers.getMapper(CostDtoMapper.class);

    Cost costCreateDtoToCost(CostDto costDto);

    @Mapping(source = "value", target = "value")
    CostDto costToCostDto(Cost cost);

}
