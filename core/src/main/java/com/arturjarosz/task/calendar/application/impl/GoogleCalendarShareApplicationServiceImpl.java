package com.arturjarosz.task.calendar.application.impl;

import com.arturjarosz.task.calendar.application.CalendarSyncService;
import com.arturjarosz.task.calendar.application.GoogleCalendarShareApplicationService;
import com.arturjarosz.task.calendar.application.GoogleCalendarShareValidator;
import com.arturjarosz.task.calendar.domain.GoogleCalendarService;
import com.arturjarosz.task.dto.CalendarShareDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.systemparameter.application.GoogleCalendarSettingsProvider;
import com.arturjarosz.task.systemparameter.application.dto.GoogleCalendarSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class GoogleCalendarShareApplicationServiceImpl implements GoogleCalendarShareApplicationService {

    private final GoogleCalendarSettingsProvider googleCalendarSettingsProvider;
    private final GoogleCalendarService googleCalendarService;
    private final CalendarSyncService calendarSyncService;

    @Override
    public List<CalendarShareDto> getShares() {
        var settings = this.getActiveSettings();
        return this.getPeopleShares(this.getGoogleCalendarId(), settings);
    }

    @Override
    public CalendarShareDto addShare(CalendarShareDto calendarShareDto) {
        GoogleCalendarShareValidator.validateCalendarShareDto(calendarShareDto);
        this.getActiveSettings();
        var googleCalendarId = this.getGoogleCalendarId();
        var email = normalizeEmail(calendarShareDto.getEmail());
        // checked against all shares, so the service account and the calendar's own address cannot lose their access
        GoogleCalendarShareValidator.validateNotShared(this.googleCalendarService.listShares(googleCalendarId), email);
        var share = this.googleCalendarService.shareCalendar(googleCalendarId, email, calendarShareDto.getRole());
        LOG.info("Google Calendar shared with role {}.", share.getRole());
        return share;
    }

    @Override
    public void removeShare(String email) {
        GoogleCalendarShareValidator.validateEmailNotEmpty(email);
        var settings = this.getActiveSettings();
        var googleCalendarId = this.getGoogleCalendarId();
        var normalizedEmail = normalizeEmail(email);
        GoogleCalendarShareValidator.validateShared(this.getPeopleShares(googleCalendarId, settings), normalizedEmail);
        this.googleCalendarService.revokeShare(googleCalendarId, normalizedEmail);
        LOG.info("Access to Google Calendar revoked.");
    }

    // Google stores emails in lower case and uses them in ids of access rules
    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private GoogleCalendarSettings getActiveSettings() {
        var maybeSettings = this.googleCalendarSettingsProvider.getActiveSettings();
        GoogleCalendarShareValidator.validateSyncActive(maybeSettings);
        return maybeSettings.get();
    }

    private String getGoogleCalendarId() {
        var maybeGoogleCalendarId = this.calendarSyncService.getGoogleCalendarId();
        GoogleCalendarShareValidator.validateCalendarExistence(maybeGoogleCalendarId);
        return maybeGoogleCalendarId.get();
    }

    // The service account owns the calendar and keeps it synchronized, and Google lists the calendar's own address
    // as an owner too; neither is a person, so they are not shown and cannot be revoked
    private List<CalendarShareDto> getPeopleShares(String googleCalendarId, GoogleCalendarSettings settings) {
        var serviceAccountEmail = this.googleCalendarService.readServiceAccountEmail(settings.credentials())
                .orElse(null);
        return this.googleCalendarService.listShares(googleCalendarId).stream()
                .filter(share -> !share.getEmail().equalsIgnoreCase(serviceAccountEmail)
                        && !share.getEmail().equalsIgnoreCase(googleCalendarId))
                .toList();
    }
}
