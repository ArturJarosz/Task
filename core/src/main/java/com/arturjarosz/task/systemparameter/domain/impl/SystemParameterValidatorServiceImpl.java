package com.arturjarosz.task.systemparameter.domain.impl;

import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameterValidatorService;
import com.arturjarosz.task.systemparameter.domain.validator.SystemParameterValidator;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@DomainService
public class SystemParameterValidatorServiceImpl implements SystemParameterValidatorService {
    private final Map<String, SystemParameterValidator> systemNameToValidator;
    private final SystemParameterQueryService systemParameterQueryService;

    @Autowired
    public SystemParameterValidatorServiceImpl(List<SystemParameterValidator> validators,
                                               SystemParameterQueryService systemParameterQueryService) {
        this.systemNameToValidator = validators.stream()
                .collect(Collectors.toMap(SystemParameterValidator::getSystemParameterName, Function.identity()));
        this.systemParameterQueryService = systemParameterQueryService;
        this.validateSystemParameters();
    }

    @Override
    public void validateSystemParameters() {
        List<String> systemParametersNames = this.systemParameterQueryService.getSystemParametersNames();
        systemParametersNames.forEach(systemParameterName -> {
            SystemParameterValidator validator = this.systemNameToValidator.get(systemParameterName);
            assertNotNull(validator,
                    createMessageCode(ExceptionCodes.NOT_EXIST, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                            SystemParameterExceptionCodes.VALIDATOR), systemParameterName);
            validator.validate(systemParameterName);
        });
    }
}
