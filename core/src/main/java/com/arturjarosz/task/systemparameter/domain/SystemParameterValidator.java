package com.arturjarosz.task.systemparameter.domain;

public interface SystemParameterValidator {

    /**
     * Validates system parameter with given name.
     */
    void validate(String name);
}
