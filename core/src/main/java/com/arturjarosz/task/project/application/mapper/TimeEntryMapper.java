package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.dto.TimeEntryDto;
import com.arturjarosz.task.project.model.TimeEntry;
import org.mapstruct.Mapper;

@Mapper
public interface TimeEntryMapper {

    TimeEntryDto mapToDto(TimeEntry timeEntry);
}
