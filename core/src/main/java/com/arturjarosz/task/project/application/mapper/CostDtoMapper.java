package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.project.application.dto.CostDto;
import com.arturjarosz.task.project.model.Cost;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CostDtoMapper {
    CostDtoMapper INSTANCE = Mappers.getMapper(CostDtoMapper.class);

    Cost costCreateDtoToCost(CostDto costDto);

    CostDto costToCostDto(Cost cost);

}
