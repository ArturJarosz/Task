package com.arturjarosz.task.calendar

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.CalendarShareDto
import com.arturjarosz.task.dto.CalendarShareRoleDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

// Google cannot be reached from tests, so only requests refused before calling Google are checked here
class GoogleCalendarShareTestIT extends BaseTestIT {
    static final String CALENDAR_SHARES_URI = "/calendars/shares"
    static final String EMAIL = "john.doe@gmail.com"
    static final ObjectMapper MAPPER = new ObjectMapper()

    @Autowired
    MockMvc mockMvc

    def "Getting shares should return 400 when sync is turned off"() {
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + CALENDAR_SHARES_URI))).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            response.contentAsString.contains("Turn Google Calendar sync on")
    }

    def "Sharing the calendar should return 400 for #description"() {
        when:
            def response = this.addShare(new CalendarShareDto(email: email, role: role))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        where:
            description            | email      | role
            "invalid email"        | "john.doe" | CalendarShareRoleDto.READER
            "missing role"         | EMAIL      | null
            "owner role"           | EMAIL      | CalendarShareRoleDto.OWNER
            "turned off sync"      | EMAIL      | CalendarShareRoleDto.WRITER
    }

    def "Revoking access should return 400 when sync is turned off"() {
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .delete(URI.create(HOST + ":" + port + CALENDAR_SHARES_URI + "?email=" + EMAIL)))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
    }

    private def addShare(CalendarShareDto calendarShareDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders
                .post(URI.create(HOST + ":" + port + CALENDAR_SHARES_URI))
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(calendarShareDto))).andReturn().response
    }
}
