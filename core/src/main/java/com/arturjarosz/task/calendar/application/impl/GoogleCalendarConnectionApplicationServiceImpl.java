package com.arturjarosz.task.calendar.application.impl;

import com.arturjarosz.task.calendar.application.CalendarSyncService;
import com.arturjarosz.task.calendar.application.GoogleCalendarConnectionApplicationService;
import com.arturjarosz.task.calendar.domain.GoogleCalendarService;
import com.arturjarosz.task.dto.GoogleCalendarConnectionDto;
import com.arturjarosz.task.dto.GoogleCalendarConnectionStatusDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.systemparameter.application.GoogleCalendarSettingsProvider;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@ApplicationService
public class GoogleCalendarConnectionApplicationServiceImpl implements GoogleCalendarConnectionApplicationService {

    private final GoogleCalendarSettingsProvider googleCalendarSettingsProvider;
    private final GoogleCalendarService googleCalendarService;
    private final CalendarSyncService calendarSyncService;

    @Override
    public GoogleCalendarConnectionDto getConnectionStatus() {
        var settings = this.googleCalendarSettingsProvider.getSettings();
        var connectionDto = new GoogleCalendarConnectionDto();
        connectionDto.setCheckedDateTime(LocalDateTime.now().toString());
        if (!settings.hasCredentials()) {
            connectionDto.setStatus(GoogleCalendarConnectionStatusDto.NOT_CONFIGURED);
            return connectionDto;
        }
        connectionDto.setServiceAccountEmail(
                this.googleCalendarService.readServiceAccountEmail(settings.credentials()).orElse(null));
        if (!settings.syncEnabled()) {
            connectionDto.setStatus(GoogleCalendarConnectionStatusDto.DISABLED);
            return connectionDto;
        }
        var result = this.googleCalendarService.checkConnection(settings,
                this.calendarSyncService.getGoogleCalendarId().orElse(null));
        connectionDto.setStatus(result.status());
        connectionDto.setMessage(result.message());
        return connectionDto;
    }
}
