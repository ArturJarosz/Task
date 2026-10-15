package com.arturjarosz.task.calendar.domain;

/**
 * Thrown when the Google Calendar rejects the stored sync token and a full synchronization is required.
 */
public class SyncTokenExpiredException extends RuntimeException {

    public SyncTokenExpiredException(Throwable cause) {
        super("Google Calendar sync token expired, full synchronization is required.", cause);
    }
}
