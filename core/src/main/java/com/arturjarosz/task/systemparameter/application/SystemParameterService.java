package com.arturjarosz.task.systemparameter.application;

import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto;
import com.arturjarosz.task.systemparameter.model.SystemParameter;

import java.util.List;

public interface SystemParameterService {

    SystemParameterDto createSystemParameter(SystemParameterDto systemParameterDto, SystemParameter systemParameter);

    SystemParameterDto updateSystemParameter(Long systemParameterId, SystemParameterDto systemParameterDto);

    void removeSystemParameter(Long systemParameterId);

    SystemParameterDto getSystemParameter(Long systemParameterId);

    List<SystemParameterDto> getSystemParameters();
}
