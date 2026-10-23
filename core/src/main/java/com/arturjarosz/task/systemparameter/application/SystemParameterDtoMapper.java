package com.arturjarosz.task.systemparameter.application;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.systemparameter.model.SystemParameter;
import com.arturjarosz.task.systemparameter.model.SystemParameterType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SystemParameterDtoMapper {
    SystemParameterDtoMapper MAPPER = Mappers.getMapper(SystemParameterDtoMapper.class);

    SystemParameterDto systemParameterToSystemParameterDto(SystemParameter systemParameter);

    /**
     * Prepares given dto to be sent to the client: fills valueSet and removes values of SECRET parameters.
     */
    static SystemParameterDto hideSecretValue(SystemParameterDto systemParameterDto) {
        if (systemParameterDto == null) {
            return null;
        }
        var value = systemParameterDto.getValue();
        systemParameterDto.setValueSet(value != null && !value.isBlank());
        if (SystemParameterType.SECRET.name().equals(systemParameterDto.getType())) {
            systemParameterDto.setValue(null);
            systemParameterDto.setDefaultValue(null);
        }
        return systemParameterDto;
    }
}
