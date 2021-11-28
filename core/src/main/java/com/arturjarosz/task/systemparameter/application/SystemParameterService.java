package com.arturjarosz.task.systemparameter.application;

import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto;

import java.util.List;

public interface SystemParameterService {

    SystemParameterDto updateSystemParameter(Long systemParameterId, SystemParameterDto systemParameterDto);

    void removeSystemParameter(Long systemParameterId);

    SystemParameterDto getSystemParameter(Long systemParameterId);

    List<SystemParameterDto> getSystemParameters();
}
