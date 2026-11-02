package com.arturjarosz.task.releasenote.application.mapper;

import com.arturjarosz.task.dto.ReleaseNoteDto;
import com.arturjarosz.task.dto.UnseenReleaseNotesDto;
import com.arturjarosz.task.releasenote.model.ReleaseNote;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReleaseNoteMapper {

    ReleaseNoteDto mapToDto(ReleaseNote releaseNote);

    UnseenReleaseNotesDto mapToUnseenDto(LocalDateTime since, List<ReleaseNoteDto> releaseNotes);
}
