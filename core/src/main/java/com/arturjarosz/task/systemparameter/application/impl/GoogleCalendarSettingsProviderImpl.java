package com.arturjarosz.task.systemparameter.application.impl;

import com.arturjarosz.task.properties.GoogleCalendarProperties;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.systemparameter.application.GoogleCalendarSettingsProvider;
import com.arturjarosz.task.systemparameter.application.dto.GoogleCalendarSettings;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ApplicationService
public class GoogleCalendarSettingsProviderImpl implements GoogleCalendarSettingsProvider {
    private final SystemParameterQueryService systemParameterQueryService;
    private final Map<String, String> reportedInvalidValues = new ConcurrentHashMap<>();

    @Autowired
    public GoogleCalendarSettingsProviderImpl(SystemParameterQueryService systemParameterQueryService) {
        this.systemParameterQueryService = systemParameterQueryService;
    }

    @Override
    public GoogleCalendarSettings getSettings() {
        var values = this.systemParameterQueryService.getValuesByNames(
                List.of(SystemParameters.GOOGLE_CALENDAR_SYNC, SystemParameters.GOOGLE_CALENDAR_CREDENTIALS,
                        SystemParameters.GOOGLE_CALENDAR_APPLICATION_NAME, SystemParameters.GOOGLE_CALENDAR_NAME));
        return new GoogleCalendarSettings(Boolean.parseBoolean(values.get(SystemParameters.GOOGLE_CALENDAR_SYNC)),
                values.get(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS),
                values.get(SystemParameters.GOOGLE_CALENDAR_APPLICATION_NAME),
                values.get(SystemParameters.GOOGLE_CALENDAR_NAME));
    }

    @Override
    public Optional<GoogleCalendarSettings> getActiveSettings() {
        return Optional.of(this.getSettings())
                .filter(settings -> settings.syncEnabled() && settings.hasCredentials());
    }

    @Override
    public Duration getPollInterval() {
        return this.getMinutes(SystemParameters.GOOGLE_CALENDAR_POLL_INTERVAL,
                GoogleCalendarProperties.DEFAULT_POLL_INTERVAL);
    }

    @Override
    public Duration getPushRetryInterval() {
        return this.getMinutes(SystemParameters.GOOGLE_CALENDAR_PUSH_RETRY_INTERVAL,
                GoogleCalendarProperties.DEFAULT_PUSH_RETRY_INTERVAL);
    }

    // The jobs must keep running, so a missing or broken value falls back to the default; the jobs read the value
    // before every run, so a broken value is reported only once
    private Duration getMinutes(String name, int defaultMinutes) {
        var value = this.systemParameterQueryService.getValueByName(name);
        try {
            var minutes = Integer.parseInt(value);
            if (minutes >= GoogleCalendarProperties.MIN_INTERVAL && minutes <= GoogleCalendarProperties.MAX_INTERVAL) {
                this.reportedInvalidValues.remove(name);
                return Duration.ofMinutes(minutes);
            }
        } catch (NumberFormatException e) {
            // reported below
        }
        if (!Objects.equals(this.reportedInvalidValues.put(name, String.valueOf(value)), String.valueOf(value))) {
            LOG.warn("System parameter '{}' has no valid number of minutes, {} minutes are used.", name,
                    defaultMinutes);
        }
        return Duration.ofMinutes(defaultMinutes);
    }
}
