package com.arturjarosz.task.releasenote

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ReleaseNoteDto
import com.arturjarosz.task.dto.ReleaseNoteTypeDto
import com.arturjarosz.task.dto.ReleaseNotesSeenDto
import com.arturjarosz.task.dto.UnseenReleaseNotesDto
import com.arturjarosz.task.user.infrastructure.repository.UserRepository
import com.arturjarosz.task.user.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ReleaseNoteTestIT extends BaseTestIT {
    static final String RELEASE_NOTES_URI = "/release-notes"
    static final String UNSEEN_URI = RELEASE_NOTES_URI + "/unseen"
    static final String SEEN_URI = RELEASE_NOTES_URI + "/seen"
    static final String CURRENT_USER_URI = "/users/current"
    static final String AUTH_ID = "auth0|release-note-test"
    static final String M2M_AUTH_ID = "release-note-test-client@clients"
    // Notes added by the tests, told apart from the ones Liquibase adds
    static final String TEST_TICKET = "TA-TEST"
    // Note Liquibase adds for the story that introduced release notes
    static final String FIRST_NOTE_TITLE = "See what's new after you log in"
    static final ObjectMapper MAPPER = new ObjectMapper()

    @Autowired
    MockMvc mockMvc

    @Autowired
    UserRepository userRepository

    @Autowired
    JdbcTemplate jdbcTemplate

    def cleanup() {
        SecurityContextHolder.clearContext()
        this.userRepository.findByAuthId(AUTH_ID).ifPresent { this.userRepository.delete(it) }
        this.jdbcTemplate.update("DELETE FROM RELEASE_NOTE WHERE TICKET = ?", TEST_TICKET)
    }

    def "Getting release notes should return all notes, including ones added by Liquibase, newest first"() {
        given:
            def published = LocalDateTime.now()
            def olderId = this.insertNote("Older note", ReleaseNoteTypeDto.FIXED, published)
            def newerId = this.insertNote("Newer note", ReleaseNoteTypeDto.NEW, published)
        when:
            def response = this.get(RELEASE_NOTES_URI)
        then:
            response.status == HttpStatus.OK.value()
        and:
            def notes = this.readNotes(response)
            notes.take(2)*.id == [newerId, olderId]
            notes[0].type == ReleaseNoteTypeDto.NEW
            notes[0].description == "Description of Newer note"
            notes[0].createdDateTime != null
            notes*.id == notes*.id.sort(false).reverse()
            notes*.title.contains(FIRST_NOTE_TITLE)
    }

    def "Getting unseen release notes of a user who has seen none should return all notes, listed since the user was created"() {
        given:
            def userCreated = this.createUser().createdDateTime
            def olderId = this.insertNote("Older", ReleaseNoteTypeDto.IMPROVED, userCreated.plusSeconds(1))
            def newerId = this.insertNote("Newer", ReleaseNoteTypeDto.FIXED, userCreated.plusSeconds(2))
            this.authenticate(AUTH_ID)
        when:
            def response = this.get(UNSEEN_URI)
        then:
            response.status == HttpStatus.OK.value()
            def unseen = this.readUnseen(response)
            unseen.releaseNotes.take(2)*.id == [newerId, olderId]
            unseen.releaseNotes*.title.contains(FIRST_NOTE_TITLE)
            LocalDateTime.parse(unseen.since) == this.userCreatedDateTime()
    }

    def "User created on first login should have seen the release notes published before"() {
        given:
            def noteId = this.insertNote("Before user", ReleaseNoteTypeDto.NEW, LocalDateTime.now())
            this.authenticate(AUTH_ID)
        when:
            this.get(CURRENT_USER_URI)
        then:
            this.lastSeenReleaseNoteId() == noteId
            this.readUnseen(this.get(UNSEEN_URI)).releaseNotes.isEmpty()
        when:
            def laterId = this.insertNote("After user", ReleaseNoteTypeDto.NEW, LocalDateTime.now())
            def unseen = this.readUnseen(this.get(UNSEEN_URI))
        then:
            unseen.releaseNotes*.id == [laterId]
            LocalDateTime.parse(unseen.since) == this.userCreatedDateTime()
    }

    def "Getting unseen release notes should leave out notes up to the last one marked as seen and list since it"() {
        given:
            def userCreated = this.createUser().createdDateTime
            this.insertNote("First", ReleaseNoteTypeDto.NEW, userCreated.plusSeconds(1))
            // Whole seconds, so the time read back from the database is the same
            def secondPublished = userCreated.plusSeconds(2).truncatedTo(ChronoUnit.SECONDS)
            def secondId = this.insertNote("Second", ReleaseNoteTypeDto.NEW, secondPublished)
            def thirdId = this.insertNote("Third", ReleaseNoteTypeDto.NEW, userCreated.plusSeconds(3))
            this.authenticate(AUTH_ID)
        when:
            def response = this.markSeen(secondId)
        then:
            response.status == HttpStatus.OK.value()
            this.lastSeenReleaseNoteId() == secondId
        and:
            def unseen = this.readUnseen(this.get(UNSEEN_URI))
            unseen.releaseNotes*.id == [thirdId]
            LocalDateTime.parse(unseen.since) == secondPublished
    }

    def "Getting unseen release notes should list since the user was created when the note seen is older"() {
        given:
            def noteId = this.insertNote("Before user", ReleaseNoteTypeDto.NEW, LocalDateTime.now().minusDays(1))
            def userCreated = this.createUser().createdDateTime
            this.authenticate(AUTH_ID)
            this.markSeen(noteId)
            def laterId = this.insertNote("After user", ReleaseNoteTypeDto.NEW, userCreated.plusSeconds(1))
        when:
            def unseen = this.readUnseen(this.get(UNSEEN_URI))
        then:
            unseen.releaseNotes*.id == [laterId]
            LocalDateTime.parse(unseen.since) == this.userCreatedDateTime()
    }

    def "Marking an older release note as seen should keep the newer one as last seen"() {
        given:
            def userCreated = this.createUser().createdDateTime
            def olderId = this.insertNote("Older", ReleaseNoteTypeDto.NEW, userCreated.plusSeconds(1))
            def newerId = this.insertNote("Newer", ReleaseNoteTypeDto.NEW, userCreated.plusSeconds(1))
            this.authenticate(AUTH_ID)
            this.markSeen(newerId)
        when:
            def response = this.markSeen(olderId)
        then:
            response.status == HttpStatus.OK.value()
            this.lastSeenReleaseNoteId() == newerId
            this.readUnseen(this.get(UNSEEN_URI)) == new UnseenReleaseNotesDto(releaseNotes: [])
    }

    def "Marking release notes as seen when current user does not exist should create the user"() {
        given:
            def noteId = this.insertNote("Note", ReleaseNoteTypeDto.NEW, LocalDateTime.now())
            this.authenticate(AUTH_ID)
        when:
            def response = this.markSeen(noteId)
        then:
            response.status == HttpStatus.OK.value()
            this.userRepository.findByAuthId(AUTH_ID).isPresent()
            this.lastSeenReleaseNoteId() == noteId
    }

    def "Marking release note that does not exist as seen should return 404 and keep the last seen note"() {
        given:
            def userCreated = this.createUser().createdDateTime
            def noteId = this.insertNote("Note", ReleaseNoteTypeDto.NEW, userCreated.plusSeconds(1))
            this.authenticate(AUTH_ID)
            this.markSeen(noteId)
        when:
            def response = this.markSeen(noteId + 1000)
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            this.lastSeenReleaseNoteId() == noteId
    }

    def "Marking release notes as seen without id should return 400"() {
        given:
            this.createUser()
            this.authenticate(AUTH_ID)
        when:
            def response = this.markSeen(null)
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.lastSeenReleaseNoteId() == null
    }

    def "Release notes of machine-to-machine token should have no unseen notes and cannot be marked as seen"() {
        given:
            def noteId = this.insertNote("Note", ReleaseNoteTypeDto.NEW, LocalDateTime.now())
            this.authenticate(M2M_AUTH_ID, ["gty": "client-credentials"])
        when:
            def unseenResponse = this.get(UNSEEN_URI)
            def seenResponse = this.markSeen(noteId)
        then:
            unseenResponse.status == HttpStatus.OK.value()
            this.readUnseen(unseenResponse) == new UnseenReleaseNotesDto(releaseNotes: [])
            seenResponse.status == HttpStatus.NOT_FOUND.value()
            this.userRepository.findByAuthId(M2M_AUTH_ID).isEmpty()
    }

    // Saved directly, so like a user created before release notes existed, the user has seen none
    private User createUser() {
        return this.userRepository.save(new User("Release note user", "release-note@example.com", AUTH_ID))
    }

    private Long insertNote(String title, ReleaseNoteTypeDto type, LocalDateTime published) {
        return this.jdbcTemplate.queryForObject("""
                INSERT INTO RELEASE_NOTE (ID, UUID, TICKET, TYPE, TITLE, DESCRIPTION, CREATED_DATETIME)
                VALUES (nextval('release_note_sequence'), gen_random_uuid(), ?, ?, ?, ?, ?) RETURNING ID""", Long,
                TEST_TICKET, type.name(), title, "Description of " + title, published)
    }

    private Long lastSeenReleaseNoteId() {
        return this.jdbcTemplate.queryForObject("SELECT LAST_SEEN_RELEASE_NOTE_ID FROM LOCAL_USER WHERE AUTH_ID = ?",
                Long, AUTH_ID)
    }

    private LocalDateTime userCreatedDateTime() {
        return this.jdbcTemplate.queryForObject("SELECT CREATED_DATETIME FROM LOCAL_USER WHERE AUTH_ID = ?",
                LocalDateTime, AUTH_ID)
    }

    private def get(String uri) {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(HOST + ":" + port + uri)))
                .andReturn().response
    }

    private def markSeen(Long lastSeenId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + SEEN_URI))
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(new ReleaseNotesSeenDto(lastSeenId: lastSeenId))))
                .andReturn().response
    }

    private static List<ReleaseNoteDto> readNotes(def response) {
        return MAPPER.readValue(response.contentAsString, new TypeReference<List<ReleaseNoteDto>>() {})
    }

    private static UnseenReleaseNotesDto readUnseen(def response) {
        return MAPPER.readValue(response.contentAsString, UnseenReleaseNotesDto)
    }
}
