package com.arturjarosz.task.systemparameter.application;

import com.arturjarosz.task.systemparameter.application.dto.GoogleCalendarSettings;

import java.time.Duration;
import java.util.Optional;

public interface GoogleCalendarSettingsProvider {

    /**
     * Loads current Google Calendar settings stored as system parameters.
     */
    GoogleCalendarSettings getSettings();

    /**
     * Returns Google Calendar settings when the sync is turned on and a service account key is set, otherwise empty.
     */
    Optional<GoogleCalendarSettings> getActiveSettings();

    /**
     * Returns how long to wait between fetching changes from Google Calendar.
     */
    Duration getPollInterval();

    /**
     * Returns how long to wait between retries of events that were not sent to Google Calendar.
     */
    Duration getPushRetryInterval();
}
