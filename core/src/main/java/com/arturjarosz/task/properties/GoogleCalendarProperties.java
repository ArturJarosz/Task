package com.arturjarosz.task.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Initial values of the Google Calendar integration. They are copied into system parameters once, when they
 * do not exist yet; afterwards the integration is configured in the application settings. Intervals are in minutes.
 */
@ConfigurationProperties(GoogleCalendarProperties.GOOGLE_CALENDAR_PROPERTY_PATH)
public record GoogleCalendarProperties(boolean enabled, String credentialsPath, String applicationName,
        String calendarName, Integer pollInterval, Integer pushRetryInterval) {
    static final String GOOGLE_CALENDAR_PROPERTY_PATH = "task.google-calendar";
    public static final int DEFAULT_POLL_INTERVAL = 15;
    public static final int DEFAULT_PUSH_RETRY_INTERVAL = 1;
    public static final int MIN_INTERVAL = 1;
    // one day
    public static final int MAX_INTERVAL = 1440;
    private static final String DEFAULT_NAME = "Task";

    public GoogleCalendarProperties {
        applicationName = applicationName == null || applicationName.isBlank() ? DEFAULT_NAME : applicationName;
        calendarName = calendarName == null || calendarName.isBlank() ? DEFAULT_NAME : calendarName;
        pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        pushRetryInterval = pushRetryInterval == null ? DEFAULT_PUSH_RETRY_INTERVAL : pushRetryInterval;
    }
}
