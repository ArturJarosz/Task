package com.arturjarosz.task.systemparameter.domain.validator;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

public abstract class AbstractSystemParameterValidator {
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9-_ ]+");

    public void validate(String parameterName) {
        this.validateSystemParameterName(parameterName);
    }

    private void validateSystemParameterName(String parameterName) {
        assertNotNull(parameterName,
                createMessageCode(ExceptionCodes.NULL, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                        SystemParameterExceptionCodes.NAME));
        assertNotEmpty(parameterName,
                createMessageCode(ExceptionCodes.EMPTY, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                        SystemParameterExceptionCodes.NAME));
        Matcher matcher = NAME_PATTERN.matcher(parameterName);
        assertIsTrue(matcher.matches(),
                createMessageCode(ExceptionCodes.NOT_VALID, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                        SystemParameterExceptionCodes.NAME));
    }

}
