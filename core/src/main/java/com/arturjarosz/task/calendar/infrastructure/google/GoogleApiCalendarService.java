package com.arturjarosz.task.calendar.infrastructure.google;

import com.arturjarosz.task.calendar.domain.GoogleCalendarException;
import com.arturjarosz.task.calendar.domain.GoogleCalendarService;
import com.arturjarosz.task.calendar.domain.SyncTokenExpiredException;
import com.arturjarosz.task.calendar.model.CalendarEvent;
import com.arturjarosz.task.calendar.model.dto.GoogleEventChangeDto;
import com.arturjarosz.task.calendar.model.dto.GoogleEventDataDto;
import com.arturjarosz.task.calendar.model.dto.GoogleEventsSyncDto;
import com.arturjarosz.task.properties.GoogleCalendarProperties;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "task.google-calendar", name = "enabled", havingValue = "true")
public class GoogleApiCalendarService implements GoogleCalendarService {
    private static final String CANCELLED_STATUS = "cancelled";

    private final Calendar calendarClient;

    public GoogleApiCalendarService(GoogleCalendarProperties googleCalendarProperties) {
        try (var credentialsStream = new FileInputStream(googleCalendarProperties.credentialsPath())) {
            var credentials = GoogleCredentials.fromStream(credentialsStream).createScoped(CalendarScopes.CALENDAR);
            this.calendarClient = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                    .setApplicationName(googleCalendarProperties.applicationName())
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleCalendarException("Cannot create Google Calendar client.", e);
        }
    }

    private static DateTime toDateTime(LocalDateTime localDateTime) {
        return new DateTime(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    private static LocalDateTime toLocalDateTime(DateTime dateTime) {
        return Instant.ofEpochMilli(dateTime.getValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public String createCalendar(String name) {
        try {
            LOG.info("Creating calendar: {},", name);
            var calendar = new com.google.api.services.calendar.model.Calendar().setSummary(name)
                    .setTimeZone(ZoneId.systemDefault().getId());
            return this.calendarClient.calendars().insert(calendar).execute().getId();
        } catch (IOException e) {
            throw new GoogleCalendarException("Creating Google Calendar failed.", e);
        }
    }

    @Override
    public GoogleEventDataDto insertEvent(String googleCalendarId, CalendarEvent calendarEvent) {
        try {
            LOG.info("Inserting calendar event.");
            var event = this.calendarClient.events()
                    .insert(googleCalendarId, this.toGoogleEvent(calendarEvent))
                    .execute();
            return new GoogleEventDataDto(event.getId(), event.getEtag());
        } catch (IOException e) {
            throw new GoogleCalendarException("Inserting event into Google Calendar failed.", e);
        }
    }

    @Override
    public GoogleEventDataDto updateEvent(String googleCalendarId, CalendarEvent calendarEvent) {
        try {
            LOG.info("Updating calendar event.");
            var event = this.calendarClient.events()
                    .update(googleCalendarId, calendarEvent.getGoogleEventId(), this.toGoogleEvent(calendarEvent))
                    .execute();
            return new GoogleEventDataDto(event.getId(), event.getEtag());
        } catch (IOException e) {
            throw new GoogleCalendarException("Updating event in Google Calendar failed.", e);
        }
    }

    @Override
    public void deleteEvent(String googleCalendarId, String googleEventId) {
        LOG.info("Deleting calendar event.");
        try {
            this.calendarClient.events().delete(googleCalendarId, googleEventId).execute();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() != HttpStatus.NOT_FOUND.value() && e.getStatusCode() != HttpStatus.GONE.value()) {
                throw new GoogleCalendarException("Removing event from Google Calendar failed.", e);
            }
            LOG.debug("Event with id {} was already removed from Google Calendar.", googleEventId);
        } catch (IOException e) {
            throw new GoogleCalendarException("Removing event from Google Calendar failed.", e);
        }
    }

    @Override
    public GoogleEventsSyncDto listEvents(String googleCalendarId, String syncToken) {
        try {
            var eventChanges = new ArrayList<GoogleEventChangeDto>();
            String pageToken = null;
            String nextSyncToken = null;
            do {
                var request = this.calendarClient.events()
                        .list(googleCalendarId)
                        .setShowDeleted(true)
                        .setPageToken(pageToken);
                if (syncToken != null) {
                    request.setSyncToken(syncToken);
                }
                var response = request.execute();
                if (response.getItems() != null) {
                    response.getItems().forEach(event -> eventChanges.add(this.toEventChange(event)));
                }
                pageToken = response.getNextPageToken();
                if (response.getNextSyncToken() != null) {
                    nextSyncToken = response.getNextSyncToken();
                }
            } while (pageToken != null);
            return new GoogleEventsSyncDto(eventChanges, nextSyncToken);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == HttpStatus.GONE.value()) {
                throw new SyncTokenExpiredException(e);
            }
            throw new GoogleCalendarException("Listing events from Google Calendar failed.", e);
        } catch (IOException e) {
            throw new GoogleCalendarException("Listing events from Google Calendar failed.", e);
        }
    }

    private Event toGoogleEvent(CalendarEvent calendarEvent) {
        var event = new Event().setSummary(calendarEvent.getName()).setDescription(calendarEvent.getDescription());
        if (calendarEvent.isAllDay()) {
            // for all day events Google Calendar expects only dates and treats the end date as exclusive
            event.setStart(new EventDateTime().setDate(
                    DateTime.parseRfc3339(calendarEvent.getStartDateTime().toLocalDate().toString())));
            event.setEnd(new EventDateTime().setDate(
                    DateTime.parseRfc3339(calendarEvent.getEndDateTime().toLocalDate().plusDays(1).toString())));
        } else {
            event.setStart(new EventDateTime().setDateTime(toDateTime(calendarEvent.getStartDateTime()))
                    .setTimeZone(ZoneId.systemDefault().getId()));
            event.setEnd(new EventDateTime().setDateTime(toDateTime(calendarEvent.getEndDateTime()))
                    .setTimeZone(ZoneId.systemDefault().getId()));
        }
        return event;
    }

    private GoogleEventChangeDto toEventChange(Event event) {
        if (CANCELLED_STATUS.equals(event.getStatus())) {
            return new GoogleEventChangeDto(event.getId(), event.getEtag(), null, null, null, null, false, true);
        }
        var allDay = event.getStart() != null && event.getStart().getDate() != null;
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        if (allDay) {
            startDateTime = LocalDate.parse(event.getStart().getDate().toStringRfc3339()).atStartOfDay();
            endDateTime = LocalDate.parse(event.getEnd().getDate().toStringRfc3339()).minusDays(1).atStartOfDay();
        } else {
            startDateTime = toLocalDateTime(event.getStart().getDateTime());
            endDateTime = toLocalDateTime(event.getEnd().getDateTime());
        }
        return new GoogleEventChangeDto(event.getId(), event.getEtag(), event.getSummary(), event.getDescription(),
                startDateTime, endDateTime, allDay, false);
    }
}
