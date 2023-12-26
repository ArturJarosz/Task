package com.arturjarosz.task.systemparameter.application;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.model.SystemParameter;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class SystemParameterApplicationValidator {

    public void validateParameterExistence(Optional<SystemParameter> maybeSystemParameter, String name) {
        assertIsTrue(maybeSystemParameter.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                        SystemParameterExceptionCodes.NAME), name);
    }

    public void validateParameterExistence(SystemParameterDto systemParameterDto, long id) {
        assertNotNull(systemParameterDto,
                createMessageCode(ExceptionCodes.NOT_EXIST, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                        SystemParameterExceptionCodes.ID), id);
    }
}
