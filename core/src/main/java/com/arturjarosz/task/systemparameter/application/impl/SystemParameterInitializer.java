package com.arturjarosz.task.systemparameter.application.impl;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.systemparameter.domain.SystemParameterValidatorService;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.infrastructure.repository.SystemParameterRepository;
import com.arturjarosz.task.systemparameter.model.SystemParameter;
import com.arturjarosz.task.systemparameter.model.SystemParameterType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Stores default values of system parameters from configuration in the database on application start. Parameters
 * already present in the database are not overridden.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class SystemParameterInitializer implements ApplicationRunner {
    private final SystemParameterRepository systemParameterRepository;
    private final SystemParameterValidatorService systemParameterValidatorService;
    private final UserProperties userProperties;

    @Autowired
    public SystemParameterInitializer(SystemParameterRepository systemParameterRepository,
            SystemParameterValidatorService systemParameterValidatorService, UserProperties userProperties) {
        this.systemParameterRepository = systemParameterRepository;
        this.systemParameterValidatorService = systemParameterValidatorService;
        this.userProperties = userProperties;
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        this.getDefaultParameters().forEach(this::createIfMissing);
    }

    private List<DefaultParameter> getDefaultParameters() {
        return List.of(
                new DefaultParameter(SystemParameters.VAT_TAX, toDecimalText(this.userProperties.getVatTax()),
                        SystemParameterType.DECIMAL_NUMBER),
                new DefaultParameter(SystemParameters.INCOME_TAX, toDecimalText(this.userProperties.getIncomeTax()),
                        SystemParameterType.DECIMAL_NUMBER),
                new DefaultParameter(SystemParameters.THEME_COLOR, this.userProperties.getThemeColor(),
                        SystemParameterType.TEXT));
    }

    private static String toDecimalText(Double value) {
        return value == null ? null : BigDecimal.valueOf(value).toPlainString();
    }

    private void createIfMissing(DefaultParameter defaultParameter) {
        var name = defaultParameter.name();
        var value = defaultParameter.value();
        if (value == null || this.systemParameterRepository.existsByName(name)) {
            return;
        }
        var systemParameterDto = new SystemParameterDto();
        systemParameterDto.setName(name);
        systemParameterDto.setValue(value);
        this.systemParameterValidatorService.validateOnUpdate(systemParameterDto);

        this.systemParameterRepository.save(new SystemParameter(name, value, value, defaultParameter.type(), true));
        LOG.info("System parameter '{}' initialized with default value {}.", name, value);
    }

    private record DefaultParameter(String name, String value, SystemParameterType type) {
    }
}
