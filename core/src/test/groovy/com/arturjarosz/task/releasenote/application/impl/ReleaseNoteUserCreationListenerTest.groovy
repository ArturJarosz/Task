package com.arturjarosz.task.releasenote.application.impl

import com.arturjarosz.task.releasenote.query.ReleaseNoteQueryService
import com.arturjarosz.task.user.model.User
import spock.lang.Specification

class ReleaseNoteUserCreationListenerTest extends Specification {

    def releaseNoteQueryService = Mock(ReleaseNoteQueryService)

    def subject = new ReleaseNoteUserCreationListener(releaseNoteQueryService)

    def "Created user should have seen the newest release note"() {
        given:
            def user = new User("testuser", "test@example.com", "auth0|123456789")
            this.releaseNoteQueryService.getNewestReleaseNoteId() >> newestId

        when:
            this.subject.onUserCreated(user)

        then:
            user.lastSeenReleaseNoteId == newestId

        where:
            newestId << [105L, null]
    }
}
