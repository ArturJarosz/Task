package com.arturjarosz.task.systemparameter.query;

import com.arturjarosz.task.dto.SystemParameterDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SystemParameterQueryService {

    /**
     * Loads SystemPropertyDto by name. Values of SECRET parameters are not included.
     */
    SystemParameterDto getSystemPropertyByName(String name);

    /**
     * Loads value of the system parameter with given name, including values of SECRET parameters. Returns null when
     * the parameter does not exist. Only for internal use, never send the result to the client.
     */
    String getValueByName(String name);

    /**
     * Loads values of the system parameters with given names, including values of SECRET parameters. Names of
     * missing parameters are not in the result. Only for internal use, never send the result to the client.
     */
    Map<String, String> getValuesByNames(Collection<String> names);

    /**
     * Loads SystemParameterDto of given systemParameterId. Values of SECRET parameters are not included.
     */
    SystemParameterDto getSystemParameter(long systemParameterId);

    /**
     * Loads names of all system parameters.
     */
    List<String> getSystemParametersNames();

    /**
     * Loads all systemParameterDtos. Values of SECRET parameters are not included.
     */
    List<SystemParameterDto> getSystemParameters();
}


