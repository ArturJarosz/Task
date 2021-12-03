package com.arturjarosz.task.systemparameter.domain;

import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto;

public interface SystemParameterValidatorService {

    void validateSystemParameters();

    void validateOnUpdate(SystemParameterDto systemParameterDto);
}
