package com.arturjarosz.task.calendar.application.mapper;

import com.arturjarosz.task.calendar.model.CalendarEvent;
import com.arturjarosz.task.dto.CalendarEventDto;
import org.mapstruct.Mapper;

@Mapper
public interface CalendarEventMapper {

    CalendarEventDto mapToDto(CalendarEvent calendarEvent);
}
