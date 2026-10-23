package com.arturjarosz.task.calendar.application;

import com.arturjarosz.task.dto.GoogleCalendarConnectionDto;

public interface GoogleCalendarConnectionApplicationService {

    /**
     * Checks whether the Google Calendar can be reached with the settings stored as system parameters.
     */
    GoogleCalendarConnectionDto getConnectionStatus();
}
