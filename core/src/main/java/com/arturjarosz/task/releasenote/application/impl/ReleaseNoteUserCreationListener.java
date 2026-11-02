package com.arturjarosz.task.releasenote.application.impl;

import com.arturjarosz.task.releasenote.query.ReleaseNoteQueryService;
import com.arturjarosz.task.user.application.UserCreationListener;
import com.arturjarosz.task.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Marks the release notes published before a user was created as seen, so "What's new" lists only the changes made
 * after the user joined.
 */
@RequiredArgsConstructor
@Component
public class ReleaseNoteUserCreationListener implements UserCreationListener {

    private final ReleaseNoteQueryService releaseNoteQueryService;

    @Override
    public void onUserCreated(User user) {
        var newestReleaseNoteId = this.releaseNoteQueryService.getNewestReleaseNoteId();
        if (newestReleaseNoteId != null) {
            user.markReleaseNotesSeen(newestReleaseNoteId);
        }
    }
}
