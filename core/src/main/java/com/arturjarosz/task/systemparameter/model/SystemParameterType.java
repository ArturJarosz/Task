package com.arturjarosz.task.systemparameter.model;

public enum SystemParameterType {
    TEXT, NUMBER, DECIMAL_NUMBER, BOOLEAN,
    // value is never returned by the API, only whether it is set
    SECRET
}
