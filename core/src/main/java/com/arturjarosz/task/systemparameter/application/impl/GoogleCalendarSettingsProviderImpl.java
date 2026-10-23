package com.arturjarosz.task.systemparameter.application.impl;

import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.systemparameter.application.GoogleCalendarSettingsProvider;
import com.arturjarosz.task.systemparameter.application.dto.GoogleCalendarSettings;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@ApplicationService
public class GoogleCalendarSettingsProviderImpl implements GoogleCalendarSettingsProvider {
    private final SystemParameterQueryService systemParameterQueryService;

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
}
