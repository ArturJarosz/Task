package com.arturjarosz.task.calendar.infrastructure.repository;

import com.arturjarosz.task.calendar.model.CalendarEvent;
import com.arturjarosz.task.calendar.model.CalendarEventSyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    Optional<CalendarEvent> findByGoogleEventId(String googleEventId);

    List<CalendarEvent> findBySyncStatus(CalendarEventSyncStatus syncStatus);

    List<CalendarEvent> findBySyncStatusNot(CalendarEventSyncStatus syncStatus);

    List<CalendarEvent> findBySyncStatusNotAndEndDateTimeGreaterThanEqualAndStartDateTimeLessThan(
            CalendarEventSyncStatus syncStatus, LocalDateTime rangeStart, LocalDateTime rangeEnd);
}
