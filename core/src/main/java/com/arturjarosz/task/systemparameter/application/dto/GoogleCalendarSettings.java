package com.arturjarosz.task.systemparameter.application.dto;

/**
 * Settings of the Google Calendar integration stored as system parameters. Credentials hold the service account JSON
 * key, empty when it is not set.
 */
public record GoogleCalendarSettings(boolean syncEnabled, String credentials, String applicationName,
        String calendarName) {

    public boolean hasCredentials() {
        return this.credentials != null && !this.credentials.isBlank();
    }
}
