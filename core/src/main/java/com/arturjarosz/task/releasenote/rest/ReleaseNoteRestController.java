package com.arturjarosz.task.releasenote.rest;

import com.arturjarosz.task.dto.ReleaseNoteDto;
import com.arturjarosz.task.dto.ReleaseNotesSeenDto;
import com.arturjarosz.task.dto.UnseenReleaseNotesDto;
import com.arturjarosz.task.releasenote.application.ReleaseNoteApplicationService;
import com.arturjarosz.task.rest.ReleaseNoteApi;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ReleaseNoteRestController implements ReleaseNoteApi {

    @NonNull
    private final ReleaseNoteApplicationService releaseNoteApplicationService;

    @Override
    public ResponseEntity<List<ReleaseNoteDto>> getReleaseNotes() {
        return new ResponseEntity<>(this.releaseNoteApplicationService.getReleaseNotes(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UnseenReleaseNotesDto> getUnseenReleaseNotes() {
        return new ResponseEntity<>(this.releaseNoteApplicationService.getUnseenReleaseNotes(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> markReleaseNotesSeen(ReleaseNotesSeenDto releaseNotesSeenDto) {
        this.releaseNoteApplicationService.markReleaseNotesSeen(releaseNotesSeenDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
