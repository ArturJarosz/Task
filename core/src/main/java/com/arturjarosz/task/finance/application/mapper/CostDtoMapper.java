package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.finance.model.Cost;
import com.arturjarosz.task.project.application.dto.CostDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CostDtoMapper {
    CostDtoMapper INSTANCE = Mappers.getMapper(CostDtoMapper.class);

    Cost costCreateDtoToCost(CostDto costDto);

    @Mapping(source = "value", target = "value")
    CostDto costToCostDto(Cost cost);

}
