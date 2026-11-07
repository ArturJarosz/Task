package com.arturjarosz.task.systemparameter.application.impl;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.properties.GoogleCalendarProperties;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Stores default values of system parameters from configuration in the database on application start. Values of
 * parameters already present in the database are not overridden.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class SystemParameterInitializer implements ApplicationRunner {
    private final SystemParameterRepository systemParameterRepository;
    private final SystemParameterValidatorService systemParameterValidatorService;
    private final UserProperties userProperties;
    private final GoogleCalendarProperties googleCalendarProperties;

    @Autowired
    public SystemParameterInitializer(SystemParameterRepository systemParameterRepository,
            SystemParameterValidatorService systemParameterValidatorService, UserProperties userProperties,
            GoogleCalendarProperties googleCalendarProperties) {
        this.systemParameterRepository = systemParameterRepository;
        this.systemParameterValidatorService = systemParameterValidatorService;
        this.userProperties = userProperties;
        this.googleCalendarProperties = googleCalendarProperties;
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        this.getDefaultParameters().forEach(this::createIfMissing);
        this.initializeGoogleCalendarParameters();
    }

    /**
     * Google Calendar parameters start from application properties, so installations configured with a credentials
     * file keep working. The sync starts turned on only when the key from that file is valid. Default values of the
     * calendar and application names follow the properties on every start.
     */
    private void initializeGoogleCalendarParameters() {
        var credentialsCreated = this.createSecretIfMissing(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS,
                this.readGoogleCredentials());
        this.createIfMissing(new DefaultParameter(SystemParameters.GOOGLE_CALENDAR_NAME,
                this.googleCalendarProperties.calendarName(), SystemParameterType.TEXT));
        this.createIfMissing(new DefaultParameter(SystemParameters.GOOGLE_CALENDAR_APPLICATION_NAME,
                this.googleCalendarProperties.applicationName(), SystemParameterType.TEXT));
        this.refreshDefaultValue(SystemParameters.GOOGLE_CALENDAR_NAME, this.googleCalendarProperties.calendarName());
        this.refreshDefaultValue(SystemParameters.GOOGLE_CALENDAR_APPLICATION_NAME,
                this.googleCalendarProperties.applicationName());
        this.createIfMissing(new DefaultParameter(SystemParameters.GOOGLE_CALENDAR_POLL_INTERVAL,
                String.valueOf(this.googleCalendarProperties.pollInterval()), SystemParameterType.NUMBER));
        this.createIfMissing(new DefaultParameter(SystemParameters.GOOGLE_CALENDAR_PUSH_RETRY_INTERVAL,
                String.valueOf(this.googleCalendarProperties.pushRetryInterval()), SystemParameterType.NUMBER));
        if (this.systemParameterRepository.existsByName(SystemParameters.GOOGLE_CALENDAR_SYNC)) {
            return;
        }
        var syncEnabled = this.googleCalendarProperties.enabled() && credentialsCreated;
        // the default stays off: turning the sync on needs a key, which is not part of the defaults
        this.systemParameterRepository.save(new SystemParameter(SystemParameters.GOOGLE_CALENDAR_SYNC,
                String.valueOf(syncEnabled), String.valueOf(false), SystemParameterType.BOOLEAN, true));
        LOG.info("System parameter '{}' initialized with value {}.", SystemParameters.GOOGLE_CALENDAR_SYNC,
                syncEnabled);
    }

    /**
     * Keeps the default value of a parameter that already exists equal to the configuration, its value stays as it is.
     * A configured default that is not valid is skipped, so it does not stop the application from starting.
     */
    private void refreshDefaultValue(String name, String defaultValue) {
        var systemParameter = this.systemParameterRepository.findByName(name)
                .filter(parameter -> !defaultValue.equals(parameter.getDefaultValue()));
        if (systemParameter.isEmpty()) {
            return;
        }
        var systemParameterDto = new SystemParameterDto();
        systemParameterDto.setName(name);
        systemParameterDto.setValue(defaultValue);
        try {
            this.systemParameterValidatorService.validateOnUpdate(systemParameterDto);
        } catch (IllegalArgumentException e) {
            LOG.warn("Default value of system parameter '{}' from configuration is not valid, it is not updated.", name);
            return;
        }
        systemParameter.get().updateDefaultValue(defaultValue);
        this.systemParameterRepository.save(systemParameter.get());
        LOG.info("Default value of system parameter '{}' updated to {}.", name, defaultValue);
    }

    /**
     * Creates SECRET parameter with given value, or empty when the value is not valid. Secret values are never
     * stored as default values and never logged. Returns whether a non-empty value was stored.
     */
    private boolean createSecretIfMissing(String name, String value) {
        if (this.systemParameterRepository.existsByName(name)) {
            return false;
        }
        var storedValue = "";
        if (value != null && !value.isBlank()) {
            var systemParameterDto = new SystemParameterDto();
            systemParameterDto.setName(name);
            systemParameterDto.setValue(value);
            try {
                this.systemParameterValidatorService.validateOnUpdate(systemParameterDto);
                storedValue = value;
            } catch (IllegalArgumentException e) {
                LOG.warn("Default value of system parameter '{}' is not valid, it is initialized empty.", name);
            }
        }
        this.systemParameterRepository.save(new SystemParameter(name, storedValue, "", SystemParameterType.SECRET,
                true));
        LOG.info("System parameter '{}' initialized, value set: {}.", name, !storedValue.isEmpty());
        return !storedValue.isEmpty();
    }

    private String readGoogleCredentials() {
        var credentialsPath = this.googleCalendarProperties.credentialsPath();
        if (credentialsPath == null || credentialsPath.isBlank()) {
            return null;
        }
        try {
            return Files.readString(Path.of(credentialsPath));
        } catch (IOException e) {
            LOG.warn("Google Calendar credentials cannot be read from {}.", credentialsPath, e);
            return null;
        }
    }

    private List<DefaultParameter> getDefaultParameters() {
        return List.of(
                new DefaultParameter(SystemParameters.VAT_TAX, toDecimalText(this.userProperties.getVatTax()),
                        SystemParameterType.DECIMAL_NUMBER),
                new DefaultParameter(SystemParameters.INCOME_TAX, toDecimalText(this.userProperties.getIncomeTax()),
                        SystemParameterType.DECIMAL_NUMBER),
                new DefaultParameter(SystemParameters.THEME_COLOR, this.userProperties.getThemeColor(),
                        SystemParameterType.TEXT),
                new DefaultParameter(SystemParameters.UPCOMING_EVENTS_SHOWN,
                        String.valueOf(this.userProperties.getUpcomingEventsShown()), SystemParameterType.NUMBER),
                new DefaultParameter(SystemParameters.PAST_EVENTS_SHOWN,
                        String.valueOf(this.userProperties.getPastEventsShown()), SystemParameterType.NUMBER));
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
