package com.arturjarosz.task.systemparameter.application;

import com.arturjarosz.task.systemparameter.application.dto.GoogleCalendarSettings;

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
}
