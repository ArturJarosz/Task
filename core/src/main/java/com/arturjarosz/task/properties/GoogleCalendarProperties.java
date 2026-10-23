package com.arturjarosz.task.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Initial values of the Google Calendar integration. They are copied into system parameters once, when they
 * do not exist yet; afterwards the integration is configured in the application settings.
 */
@ConfigurationProperties(GoogleCalendarProperties.GOOGLE_CALENDAR_PROPERTY_PATH)
public record GoogleCalendarProperties(boolean enabled, String credentialsPath, String applicationName,
        String calendarName) {
    static final String GOOGLE_CALENDAR_PROPERTY_PATH = "task.google-calendar";
    private static final String DEFAULT_NAME = "Task";

    public GoogleCalendarProperties {
        applicationName = applicationName == null || applicationName.isBlank() ? DEFAULT_NAME : applicationName;
        calendarName = calendarName == null || calendarName.isBlank() ? DEFAULT_NAME : calendarName;
    }
}
