package com.arturjarosz.task.project.application;

import com.arturjarosz.task.architect.application.ArchitectValidator;
import com.arturjarosz.task.dto.TimeEntryDto;
import com.arturjarosz.task.project.query.TimeEntryQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@RequiredArgsConstructor
@Component
public class TimeEntryValidator {

    public static final int MAX_DURATION_MINUTES = 24 * 60;
    public static final int MAX_NOTE_LENGTH = 255;
    // Work dates are calendar days in Poland, where the app is used; the server's own time zone can be behind it
    private static final ZoneId WORK_DATE_ZONE = ZoneId.of("Europe/Warsaw");

    private final ArchitectValidator architectValidator;
    private final TimeEntryQueryService timeEntryQueryService;

    public void validateTimeEntryDto(TimeEntryDto timeEntryDto) {
        assertNotNull(timeEntryDto, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.TIME_ENTRY));
        this.validateDuration(timeEntryDto.getDurationMinutes());
        this.validateWorkDate(timeEntryDto.getWorkDate());
        assertNotNull(timeEntryDto.getArchitectId(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.TIME_ENTRY,
                        ProjectExceptionCodes.ARCHITECT));
        this.validateNote(timeEntryDto.getNote());
        this.architectValidator.validateArchitectExistence(timeEntryDto.getArchitectId());
    }

    public void validateExistenceOfTimeEntryInTask(Long taskId, Long timeEntryId) {
        assertEntityPresent(this.timeEntryQueryService.timeEntryExistsInTask(taskId, timeEntryId),
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.TASK,
                        ProjectExceptionCodes.TIME_ENTRY), taskId, timeEntryId);
    }

    private void validateDuration(Integer durationMinutes) {
        assertNotNull(durationMinutes, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.TIME_ENTRY,
                ProjectExceptionCodes.DURATION));
        assertIsTrue(durationMinutes > 0, createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.TIME_ENTRY,
                ProjectExceptionCodes.DURATION));
        assertIsTrue(durationMinutes <= MAX_DURATION_MINUTES,
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.TIME_ENTRY,
                        ProjectExceptionCodes.DURATION, ProjectExceptionCodes.TOO_LONG));
    }

    private void validateWorkDate(LocalDate workDate) {
        assertNotNull(workDate, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.TIME_ENTRY,
                ProjectExceptionCodes.WORK_DATE));
        assertIsTrue(!workDate.isAfter(LocalDate.now(WORK_DATE_ZONE)),
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.TIME_ENTRY,
                        ProjectExceptionCodes.WORK_DATE));
    }

    private void validateNote(String note) {
        assertIsTrue(note == null || note.length() <= MAX_NOTE_LENGTH,
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.TIME_ENTRY,
                        ProjectExceptionCodes.NOTE, ProjectExceptionCodes.TOO_LONG), MAX_NOTE_LENGTH);
    }
}
