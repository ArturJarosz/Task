package com.arturjarosz.task.releasenote.query.impl;

import com.arturjarosz.task.dto.ReleaseNoteDto;
import com.arturjarosz.task.releasenote.application.mapper.ReleaseNoteMapper;
import com.arturjarosz.task.releasenote.model.QReleaseNote;
import com.arturjarosz.task.releasenote.model.ReleaseNote;
import com.arturjarosz.task.releasenote.query.ReleaseNoteQueryService;
import com.arturjarosz.task.releasenote.query.UnseenReleaseNotes;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.querydsl.core.types.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Finder
public class ReleaseNoteQueryServiceImpl extends AbstractQueryService<QReleaseNote> implements ReleaseNoteQueryService {

    private static final QReleaseNote RELEASE_NOTE = QReleaseNote.releaseNote;

    private final ReleaseNoteMapper releaseNoteMapper;

    public ReleaseNoteQueryServiceImpl(ReleaseNoteMapper releaseNoteMapper) {
        super(RELEASE_NOTE);
        this.releaseNoteMapper = releaseNoteMapper;
    }

    @Override
    public List<ReleaseNoteDto> getReleaseNotes() {
        return this.loadNewestFirst(null).stream().map(this.releaseNoteMapper::mapToDto).toList();
    }

    // The last seen note is loaded along with the newer ones only for its publish time, which saves a second query
    @Override
    public UnseenReleaseNotes getUnseenReleaseNotes(Long lastSeenId) {
        LocalDateTime lastSeenPublished = null;
        var unseen = new ArrayList<ReleaseNoteDto>();
        for (var releaseNote : this.loadNewestFirst(lastSeenId == null ? null : RELEASE_NOTE.id.goe(lastSeenId))) {
            if (releaseNote.getId().equals(lastSeenId)) {
                lastSeenPublished = releaseNote.getCreatedDateTime();
            } else {
                unseen.add(this.releaseNoteMapper.mapToDto(releaseNote));
            }
        }
        return new UnseenReleaseNotes(lastSeenPublished, unseen);
    }

    @Override
    public Long getNewestReleaseNoteId() {
        return this.queryFromAggregate().select(RELEASE_NOTE.id.max()).fetchOne();
    }

    // Ids come from a sequence, so the highest id is the newest note; a null condition is left out by Querydsl
    private List<ReleaseNote> loadNewestFirst(Predicate condition) {
        return this.query().selectFrom(RELEASE_NOTE).where(condition).orderBy(RELEASE_NOTE.id.desc()).fetch();
    }
}
