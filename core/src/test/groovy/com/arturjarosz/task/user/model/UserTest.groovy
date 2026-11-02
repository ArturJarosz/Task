package com.arturjarosz.task.user.model

import spock.lang.Specification

class UserTest extends Specification {
    final static USERNAME = "testuser"
    final static EMAIL = "test@example.com"
    final static AUTH_ID = "auth0|123456789"
    // 😀 is one character but two UTF-16 code units
    final static EMOJI = "😀"
    final static TOO_LONG_EMAIL = "a" * 250 + "@example.com"

    def "Creating user should fill not null columns from profile values that can be stored"() {
        when:
            def user = new User(username, email, AUTH_ID)

        then:
            user.authId == AUTH_ID
            user.username == expectedUsername
            user.email == expectedEmail

        where:
            username               | email                      || expectedUsername | expectedEmail
            USERNAME               | EMAIL                      || USERNAME         | EMAIL
            null                   | EMAIL                      || EMAIL            | EMAIL
            " "                    | EMAIL                      || EMAIL            | EMAIL
            USERNAME               | null                       || USERNAME         | ""
            USERNAME               | ""                         || USERNAME         | ""
            null                   | null                       || ""               | ""
            ""                     | " "                        || ""               | ""
            "a" * 150              | null                       || "a" * 100        | ""
            "a" * 99 + EMOJI + "b" | null                       || "a" * 99 + EMOJI | ""
            null                   | EMOJI * 100                || EMOJI * 100      | EMOJI * 100
            null                   | "a" * 150 + "@example.com" || "a" * 100        | "a" * 150 + "@example.com"
            USERNAME               | TOO_LONG_EMAIL             || USERNAME         | ""
    }

    def "Updating profile should take values that can be stored"() {
        given:
            def user = new User("old name", "old@example.com", AUTH_ID)

        when:
            user.updateProfile("a" * 150, EMAIL)

        then:
            user.username == "a" * 100
            user.email == EMAIL
    }

    def "Updating profile should keep current values instead of ones that cannot be stored"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)

        when:
            user.updateProfile(username, email)

        then:
            user.username == USERNAME
            user.email == EMAIL

        where:
            username | email
            null     | null
            ""       | " "
            null     | TOO_LONG_EMAIL
    }

    def "Marking release notes as seen should keep the newest note seen"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)

        when:
            seenIds.each { user.markReleaseNotesSeen(it) }

        then:
            user.lastSeenReleaseNoteId == expectedLastSeenId

        where:
            seenIds         || expectedLastSeenId
            []              || null
            [100L]          || 100L
            [100L, 105L]    || 105L
            [105L, 100L]    || 105L
            [105L, 105L]    || 105L
    }

    def "A release note not newer than the last one seen should count as seen"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)
            seenIds.each { user.markReleaseNotesSeen(it) }

        expect:
            user.hasSeenReleaseNote(releaseNoteId) == expectedSeen

        where:
            seenIds || releaseNoteId || expectedSeen
            []      || 100L          || false
            [105L]  || 100L          || true
            [105L]  || 105L          || true
            [105L]  || 106L          || false
    }
}
