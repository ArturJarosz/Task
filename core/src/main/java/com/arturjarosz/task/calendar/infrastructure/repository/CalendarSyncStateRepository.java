package com.arturjarosz.task.calendar.infrastructure.repository;

import com.arturjarosz.task.calendar.model.CalendarSyncState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalendarSyncStateRepository extends JpaRepository<CalendarSyncState, Long> {

    Optional<CalendarSyncState> findFirstByOrderByIdAsc();
}
