package com.arturjarosz.task.calendar.infrastructure.google;

import com.arturjarosz.task.calendar.domain.GoogleCalendarException;
import com.arturjarosz.task.calendar.domain.GoogleCalendarService;
import com.arturjarosz.task.calendar.domain.SyncTokenExpiredException;
import com.arturjarosz.task.calendar.model.CalendarEvent;
import com.arturjarosz.task.calendar.model.dto.GoogleEventChangeDto;
import com.arturjarosz.task.calendar.model.dto.GoogleEventDataDto;
import com.arturjarosz.task.calendar.model.dto.GoogleEventsSyncDto;
import com.arturjarosz.task.dto.GoogleCalendarConnectionStatusDto;
import com.arturjarosz.task.systemparameter.application.GoogleCalendarSettingsProvider;
import com.arturjarosz.task.systemparameter.application.SystemParameterChangeListener;
import com.arturjarosz.task.systemparameter.application.dto.GoogleCalendarSettings;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class GoogleApiCalendarService implements GoogleCalendarService, SystemParameterChangeListener {
    private static final String CANCELLED_STATUS = "cancelled";
    private static final Set<String> CLIENT_PARAMETERS = Set.of(SystemParameters.GOOGLE_CALENDAR_SYNC,
            SystemParameters.GOOGLE_CALENDAR_CREDENTIALS, SystemParameters.GOOGLE_CALENDAR_APPLICATION_NAME);

    private final GoogleCalendarSettingsProvider googleCalendarSettingsProvider;

    // Client is built from the stored configuration on first use and dropped when that configuration changes
    private GoogleCalendarSettings clientSettings;
    private Calendar calendarClient;

    public GoogleApiCalendarService(GoogleCalendarSettingsProvider googleCalendarSettingsProvider) {
        this.googleCalendarSettingsProvider = googleCalendarSettingsProvider;
    }

    @Override
    public void onSystemParameterChanged(String name, String previousValue, String newValue) {
        if (!CLIENT_PARAMETERS.contains(name)) {
            return;
        }
        this.dropClient();
        // a client built before the change is committed would hold the previous configuration
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    GoogleApiCalendarService.this.dropClient();
                }
            });
        }
    }

    private synchronized void dropClient() {
        this.calendarClient = null;
        this.clientSettings = null;
    }

    private synchronized Calendar client() {
        if (this.calendarClient == null) {
            var settings = this.googleCalendarSettingsProvider.getActiveSettings()
                    .orElseThrow(() -> new GoogleCalendarException("Google Calendar integration is not configured.",
                            null));
            this.calendarClient = createClient(settings);
            this.clientSettings = settings;
        }
        return this.calendarClient;
    }

    private synchronized Calendar clientFor(GoogleCalendarSettings settings) {
        return settings.equals(this.clientSettings) ? this.calendarClient : createClient(settings);
    }

    private static Calendar createClient(GoogleCalendarSettings settings) {
        try (var credentialsStream = new ByteArrayInputStream(settings.credentials().getBytes(StandardCharsets.UTF_8))) {
            var credentials = GoogleCredentials.fromStream(credentialsStream).createScoped(CalendarScopes.CALENDAR);
            return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(settings.applicationName())
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
            return this.client().calendars().insert(calendar).execute().getId();
        } catch (IOException e) {
            throw new GoogleCalendarException("Creating Google Calendar failed.", e);
        }
    }

    @Override
    public void renameCalendar(String googleCalendarId, String name) {
        try {
            LOG.info("Renaming calendar to: {}", name);
            var calendar = new com.google.api.services.calendar.model.Calendar().setSummary(name);
            this.client().calendars().patch(googleCalendarId, calendar).execute();
        } catch (IOException e) {
            throw new GoogleCalendarException("Renaming Google Calendar failed.", e);
        }
    }

    @Override
    public GoogleEventDataDto insertEvent(String googleCalendarId, CalendarEvent calendarEvent) {
        try {
            LOG.info("Inserting calendar event.");
            var event = this.client().events()
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
            var event = this.client().events()
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
            this.client().events().delete(googleCalendarId, googleEventId).execute();
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
                var request = this.client().events()
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

    @Override
    public ConnectionCheckResult checkConnection(GoogleCalendarSettings settings, String googleCalendarId) {
        try {
            var client = this.clientFor(settings);
            if (googleCalendarId == null) {
                client.calendarList().list().setMaxResults(1).execute();
            } else {
                client.calendars().get(googleCalendarId).execute();
            }
            return new ConnectionCheckResult(GoogleCalendarConnectionStatusDto.CONNECTED, null);
        } catch (GoogleCalendarException e) {
            LOG.info("Google Calendar client cannot be created from stored credentials.", e);
            return new ConnectionCheckResult(GoogleCalendarConnectionStatusDto.INVALID_CREDENTIALS,
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } catch (IOException e) {
            LOG.info("Checking connection with Google Calendar failed.", e);
            return toConnectionCheckResult(e);
        }
    }

    private static ConnectionCheckResult toConnectionCheckResult(IOException exception) {
        var httpException = findCause(exception, HttpResponseException.class);
        // Calendar API errors are JSON responses; a plain HTTP error comes from the token endpoint, which rejects keys
        // that are revoked, deleted or malformed
        if (httpException != null && !(httpException instanceof GoogleJsonResponseException)) {
            return new ConnectionCheckResult(GoogleCalendarConnectionStatusDto.INVALID_CREDENTIALS,
                    httpException.getContent() != null ? httpException.getContent() : httpException.getMessage());
        }
        if (httpException == null) {
            return new ConnectionCheckResult(GoogleCalendarConnectionStatusDto.CONNECTION_FAILED,
                    exception.getMessage());
        }
        var message = httpException instanceof GoogleJsonResponseException jsonException
                && jsonException.getDetails() != null
                ? jsonException.getDetails().getMessage()
                : httpException.getStatusMessage();
        var status = switch (httpException.getStatusCode()) {
            // 400 from the Calendar API is a malformed request, rejected keys come from the token endpoint above
            case 401 -> GoogleCalendarConnectionStatusDto.INVALID_CREDENTIALS;
            case 403 -> GoogleCalendarConnectionStatusDto.PERMISSION_DENIED;
            case 404, 410 -> GoogleCalendarConnectionStatusDto.CALENDAR_NOT_FOUND;
            default -> GoogleCalendarConnectionStatusDto.CONNECTION_FAILED;
        };
        return new ConnectionCheckResult(status, message);
    }

    private static <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        for (var cause = throwable; cause != null; cause = cause.getCause()) {
            if (type.isInstance(cause)) {
                return type.cast(cause);
            }
        }
        return null;
    }

    @Override
    public Optional<String> readServiceAccountEmail(String credentials) {
        if (credentials == null || credentials.isBlank()) {
            return Optional.empty();
        }
        try (var credentialsStream = new ByteArrayInputStream(credentials.getBytes(StandardCharsets.UTF_8))) {
            return Optional.ofNullable(ServiceAccountCredentials.fromStream(credentialsStream).getClientEmail());
        } catch (IOException | RuntimeException e) {
            LOG.debug("Given credentials are not a valid service account key.", e);
            return Optional.empty();
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
