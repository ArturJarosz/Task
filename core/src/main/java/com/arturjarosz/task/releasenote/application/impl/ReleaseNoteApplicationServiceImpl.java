package com.arturjarosz.task.releasenote.application.impl;

import com.arturjarosz.task.dto.ReleaseNoteDto;
import com.arturjarosz.task.dto.ReleaseNotesSeenDto;
import com.arturjarosz.task.dto.UnseenReleaseNotesDto;
import com.arturjarosz.task.releasenote.application.ReleaseNoteApplicationService;
import com.arturjarosz.task.releasenote.application.mapper.ReleaseNoteMapper;
import com.arturjarosz.task.releasenote.domain.ReleaseNoteExceptionCodes;
import com.arturjarosz.task.releasenote.infrastructure.repository.ReleaseNoteRepository;
import com.arturjarosz.task.releasenote.query.ReleaseNoteQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.user.application.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ReleaseNoteApplicationServiceImpl implements ReleaseNoteApplicationService {

    private final CurrentUserProvider currentUserProvider;
    private final ReleaseNoteMapper releaseNoteMapper;
    private final ReleaseNoteQueryService releaseNoteQueryService;
    private final ReleaseNoteRepository releaseNoteRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ReleaseNoteDto> getReleaseNotes() {
        LOG.info("Getting release notes.");

        return this.releaseNoteQueryService.getReleaseNotes();
    }

    @Transactional(readOnly = true)
    @Override
    public UnseenReleaseNotesDto getUnseenReleaseNotes() {
        LOG.info("Getting release notes not seen by current user.");

        return this.currentUserProvider.findCurrentUser()
                .map(user -> {
                    var unseen = this.releaseNoteQueryService.getUnseenReleaseNotes(user.getLastSeenReleaseNoteId());
                    // Mostly there is nothing new, and then there is no period to show
                    var since = unseen.releaseNotes().isEmpty() ? null
                            : getUnseenSince(unseen.lastSeenPublished(), user.getCreatedDateTime());
                    return this.releaseNoteMapper.mapToUnseenDto(since, unseen.releaseNotes());
                })
                .orElseGet(() -> this.releaseNoteMapper.mapToUnseenDto(null, List.of()));
    }

    @Transactional
    @Override
    public void markReleaseNotesSeen(ReleaseNotesSeenDto releaseNotesSeenDto) {
        var lastSeenId = releaseNotesSeenDto.getLastSeenId();
        LOG.info("Marking release notes up to {} as seen by current user.", lastSeenId);

        assertNotNull(lastSeenId, createMessageCode(ExceptionCodes.NULL, ReleaseNoteExceptionCodes.RELEASE_NOTE,
                ReleaseNoteExceptionCodes.LAST_SEEN_ID));
        var user = this.currentUserProvider.getOrCreateCurrentUser();
        // Closing "What's new" again mostly sends a note already seen, which changes nothing
        if (user.hasSeenReleaseNote(lastSeenId)) {
            LOG.info("Release notes up to {} were already seen by current user.", lastSeenId);
            return;
        }
        assertEntityPresent(this.releaseNoteRepository.existsById(lastSeenId),
                createMessageCode(ExceptionCodes.NOT_EXIST, ReleaseNoteExceptionCodes.RELEASE_NOTE), lastSeenId);
        user.markReleaseNotesSeen(lastSeenId);

        LOG.info("Release notes up to {} marked as seen by current user.", lastSeenId);
    }

    // Ids follow the publishing order, so the unseen notes are the ones published after the newest note the user has
    // seen. A new user has seen the notes published before joining, and one who has seen none joined before the first
    // note, so the user's creation is the start when it is later.
    private static LocalDateTime getUnseenSince(LocalDateTime lastSeenPublished, LocalDateTime userCreated) {
        return lastSeenPublished != null && lastSeenPublished.isAfter(userCreated) ? lastSeenPublished : userCreated;
    }
}
