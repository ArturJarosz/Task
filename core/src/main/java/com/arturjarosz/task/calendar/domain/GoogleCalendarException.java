package com.arturjarosz.task.calendar.domain;

/**
 * Thrown when communication with the Google Calendar API fails.
 */
public class GoogleCalendarException extends RuntimeException {

    public GoogleCalendarException(String message, Throwable cause) {
        super(message, cause);
    }
}
