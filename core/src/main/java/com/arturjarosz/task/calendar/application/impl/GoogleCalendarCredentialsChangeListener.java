package com.arturjarosz.task.calendar.application.impl;

import com.arturjarosz.task.calendar.application.CalendarSyncService;
import com.arturjarosz.task.calendar.domain.GoogleCalendarService;
import com.arturjarosz.task.systemparameter.application.SystemParameterChangeListener;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Starts the synchronization over when the Google Calendar credentials belong to another service account than the
 * synchronized calendar: the calendar and its events belong to the previous account, which may no longer be
 * reachable.
 */
@RequiredArgsConstructor
@Component
public class GoogleCalendarCredentialsChangeListener implements SystemParameterChangeListener {

    private final GoogleCalendarService googleCalendarService;
    private final CalendarSyncService calendarSyncService;

    @Override
    public void onSystemParameterChanged(String name, String previousValue, String newValue) {
        if (!SystemParameters.GOOGLE_CALENDAR_CREDENTIALS.equals(name)) {
            return;
        }
        var newAccount = this.googleCalendarService.readServiceAccountEmail(newValue);
        // a removed key keeps the calendar, it is used again when a key of the same account is added
        if (newAccount.isEmpty()) {
            return;
        }
        var previousAccount = this.googleCalendarService.readServiceAccountEmail(previousValue);
        this.calendarSyncService.resetSynchronizationIfAccountChanged(previousAccount.orElse(null), newAccount.get());
    }
}
