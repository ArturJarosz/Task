package com.arturjarosz.task.systemparameter.application;

import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto;
import com.arturjarosz.task.systemparameter.model.SystemParameter;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SystemParameterDtoMapper {
    SystemParameterDtoMapper MAPPER = Mappers.getMapper(SystemParameterDtoMapper.class);

    SystemParameterDto systemParameterToSystemParameterDto(SystemParameter systemParameter);
}
