package com.arturjarosz.task.releasenote.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public final class ReleaseNoteExceptionCodes {
    public static final String LAST_SEEN_ID = "lastSeenId";
    public static final String RELEASE_NOTE = "releaseNote";

    private ReleaseNoteExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
