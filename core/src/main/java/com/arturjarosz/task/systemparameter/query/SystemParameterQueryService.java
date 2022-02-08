package com.arturjarosz.task.systemparameter.query;

import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto;

import java.util.List;

public interface SystemParameterQueryService {

    /**
     * Loads SystemPropertyDto by name.
     */
    SystemParameterDto getSystemPropertyByName(String name);

    /**
     * Loads SystemParameterDto of given systemParameterId
     */
    SystemParameterDto getSystemParameter(long systemParameterId);

    /**
     * Loads names of all system parameters.
     */
    List<String> getSystemParametersNames();

    /**
     * Loads all systemParameterDtos.
     */
    List<SystemParameterDto> getSystemParameters();
}


