package com.arturjarosz.task.systemparameter.query;

import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto;

public interface SystemParameterQueryService {

    /**
     * Loads SystemPropertyDto by name.
     */
    SystemParameterDto getSystemPropertyByName(String name);

}


