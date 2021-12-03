package com.arturjarosz.task.systemparameter.query;

import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto;

import java.util.List;

public interface SystemParameterQueryService {

    /**
     * Loads SystemPropertyDto by name.
     */
    SystemParameterDto getSystemPropertyByName(String name);

    SystemParameterDto getSystemParameter(long id);

    List<String> getSystemParametersNames();

    List<SystemParameterDto> getSystemParameters();
}


