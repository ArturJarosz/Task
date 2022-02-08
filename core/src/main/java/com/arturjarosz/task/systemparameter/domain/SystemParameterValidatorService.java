package com.arturjarosz.task.systemparameter.domain;

import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto;

/**
 * Service responsible for validating system parameter.
 */
public interface SystemParameterValidatorService {

    /**
     * Validates if all system parameters stored in database have validators are run them for each system parameter.
     * If parameter does not have a validator, then new exception is thrown.
     */
    void validateSystemParameters();

    /**
     * Contains code responsible for validating system parameter on update. Checks name and value of the parameter.
     */
    void validateOnUpdate(SystemParameterDto systemParameterDto);
}
