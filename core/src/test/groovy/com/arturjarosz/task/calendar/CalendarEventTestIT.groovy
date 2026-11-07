package com.arturjarosz.task.calendar

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.dto.CalendarEventDto
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.dto.ProjectCreateDto
import com.arturjarosz.task.utils.TestsHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import tools.jackson.databind.ObjectMapper

/**
 * Connections of calendar events with clients and projects.
 */
class CalendarEventTestIT extends BaseTestIT {
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String PROJECTS_URI = "/projects"
    static final String EVENTS_URI = "/calendars/events"
    static final long NOT_EXISTING_ID = 10000L
    static final ObjectMapper MAPPER = new ObjectMapper()

    final def architect = this.readJson('json/architect/architect.json', ArchitectDto)
    final def privateClientDto = this.readJson('json/client/privateClient.json', ClientDto)
    final def projectCreateDto = this.readJson('json/project/properProject.json', ProjectCreateDto)

    @Autowired
    private MockMvc mockMvc

    private Long clientId
    private Long projectId
    private Long architectId
    // Removed after each test, as other tests count all clients and projects
    private final List<Long> eventIds = []
    private final List<Long> clientIds = []

    def setup() {
        projectCreateDto.typeId = projectTypeId
        architectId = TestsHelper.createArchitect(architect, "$HOST:$port$ARCHITECTS_URI", this.mockMvc).id
        projectCreateDto.architectId = architectId
        clientId = this.createClient()
        projectCreateDto.clientId = clientId
        projectId = TestsHelper.createProject(projectCreateDto, "$HOST:$port$PROJECTS_URI", this.mockMvc).id
    }

    def cleanup() {
        // Events first, though removing a client or project would only drop their connection
        eventIds.each { this.delete("$EVENTS_URI/$it") }
        this.delete("$PROJECTS_URI/$projectId")
        clientIds.each { this.delete("$CLIENTS_URI/$it") }
        this.delete("$ARCHITECTS_URI/$architectId")
    }

    def "Creating an event with #description should save its connections"() {
        when:
            def response = this.saveEvent(withClient ? clientId : null, withProject ? projectId : null)
        then:
            response.status == HttpStatus.CREATED.value()
            def event = MAPPER.readValue(response.contentAsString, CalendarEventDto)
            event.clientId == (withClient ? clientId : null)
            event.projectId == (withProject ? projectId : null)
        where:
            description            | withClient | withProject
            "no client or project" | false      | false
            "a client"             | true       | false
            "a project"            | false      | true
            "a client and project" | true       | true
    }

    def "Creating an event with a project of another client should return code 400"() {
        when:
            def response = this.saveEvent(this.createClient(), projectId)
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
    }

    def "Creating an event with a not existing #description should return code 404"() {
        when:
            def response = this.saveEvent(notExistingClient ? NOT_EXISTING_ID : null,
                    notExistingClient ? null : NOT_EXISTING_ID)
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        where:
            description | notExistingClient
            "client"    | true
            "project"   | false
    }

    def "Updating an event should replace and clear its connections"() {
        given:
            def event = MAPPER.readValue(this.saveEvent(clientId, projectId).contentAsString, CalendarEventDto)
        when:
            def dto = eventDto(clientId, null)
            def response = this.mockMvc.perform(MockMvcRequestBuilders.put("$HOST:$port$EVENTS_URI/${event.id}")
                    .contentType("application/json").content(MAPPER.writeValueAsString(dto)))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
            def updated = this.getEvent(event.id)
            updated.clientId == clientId
            updated.projectId == null
    }

    def "Getting events of a client or project should return only events connected with it"() {
        given:
            def clientEvent = this.read(this.saveEvent(clientId, null))
            def projectEvent = this.read(this.saveEvent(null, projectId))
            def otherEvent = this.read(this.saveEvent(this.createClient(), null))
        when:
            def forClient = this.getEvents("clientId=$clientId")
            def forProject = this.getEvents("projectId=$projectId")
        then:
            forClient*.id.containsAll([clientEvent.id, projectEvent.id])
            !forClient*.id.contains(otherEvent.id)
            forProject*.id == [projectEvent.id]
    }

    def "Removing the project and client should keep the event without the connections"() {
        given:
            def event = MAPPER.readValue(this.saveEvent(clientId, projectId).contentAsString, CalendarEventDto)
        when:
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .delete("$HOST:$port$PROJECTS_URI/$projectId")).andReturn().response
            def afterProject = this.getEvent(event.id)
            def clientResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .delete("$HOST:$port$CLIENTS_URI/$clientId")).andReturn().response
            def afterClient = this.getEvent(event.id)
        then:
            projectResponse.status == HttpStatus.OK.value()
            afterProject.projectId == null
            afterProject.clientId == clientId
            clientResponse.status == HttpStatus.OK.value()
            afterClient.clientId == null
            afterClient.name == event.name
    }

    private <T> T readJson(String fileName, Class<T> valueType) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(fileName).file), valueType)
    }

    private Long createClient() {
        def id = TestsHelper.createClient(privateClientDto, "$HOST:$port$CLIENTS_URI", this.mockMvc).id
        clientIds << id
        return id
    }

    // Already removed by the test is fine
    private void delete(String uri) {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("$HOST:$port$uri")).andReturn()
    }

    private MockHttpServletResponse saveEvent(Long clientId, Long projectId) {
        def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$HOST:$port$EVENTS_URI")
                .contentType("application/json").content(MAPPER.writeValueAsString(eventDto(clientId, projectId))))
                .andReturn().response
        if (response.status == HttpStatus.CREATED.value()) {
            eventIds << MAPPER.readValue(response.contentAsString, CalendarEventDto).id
        }
        return response
    }

    private static CalendarEventDto read(MockHttpServletResponse response) {
        return MAPPER.readValue(response.contentAsString, CalendarEventDto)
    }

    private List<CalendarEventDto> getEvents(String query) {
        def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$HOST:$port$EVENTS_URI?$query"))
                .andReturn().response
        return MAPPER.readValue(response.contentAsString,
                MAPPER.getTypeFactory().constructCollectionType(List, CalendarEventDto))
    }

    private CalendarEventDto getEvent(Long eventId) {
        def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$HOST:$port$EVENTS_URI/$eventId"))
                .andReturn().response
        return MAPPER.readValue(response.contentAsString, CalendarEventDto)
    }

    private static CalendarEventDto eventDto(Long clientId, Long projectId) {
        return new CalendarEventDto(name: "Site visit", startDateTime: "2026-11-10T10:00:00",
                endDateTime: "2026-11-10T11:00:00", clientId: clientId, projectId: projectId)
    }
}
