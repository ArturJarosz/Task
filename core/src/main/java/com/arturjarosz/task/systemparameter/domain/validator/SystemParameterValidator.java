package com.arturjarosz.task.systemparameter.domain.validator;

import com.arturjarosz.task.dto.SystemParameterDto;

public interface SystemParameterValidator {

    /**
     * Validates System Parameter with given name.
     */
    void validate(String name);

    /**
     * Validates if System Parameter can be updated with data with given data in SystemParameterDto.
     */
    void validateOnUpdate(SystemParameterDto systemParameterDto);

    String getSystemParameterName();
}
