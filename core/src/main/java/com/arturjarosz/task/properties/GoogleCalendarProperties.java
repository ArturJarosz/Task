package com.arturjarosz.task.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(GoogleCalendarProperties.GOOGLE_CALENDAR_PROPERTY_PATH)
public record GoogleCalendarProperties(boolean enabled, String credentialsPath, String applicationName,
        String calendarName) {
    static final String GOOGLE_CALENDAR_PROPERTY_PATH = "task.google-calendar";
}
