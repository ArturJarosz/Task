package com.arturjarosz.task.calendar

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.GoogleCalendarConnectionDto
import com.arturjarosz.task.dto.GoogleCalendarConnectionStatusDto
import com.arturjarosz.task.dto.SystemParameterDto
import com.arturjarosz.task.systemparameter.domain.SystemParameters
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

import java.security.KeyPairGenerator

class GoogleCalendarSystemParameterTestIT extends BaseTestIT {
    static final String SYSTEM_PARAMETERS_URI = "/system-parameters"
    static final String CONNECTION_STATUS_URI = "/calendars/connection-status"
    static final String SERVICE_ACCOUNT_EMAIL = "task-calendar@task-test.iam.gserviceaccount.com"
    static final ObjectMapper MAPPER = new ObjectMapper()

    @Autowired
    MockMvc mockMvc

    @Autowired
    SystemParameterQueryService systemParameterQueryService

    def "Google Calendar parameters should be created on application start with sync turned off"() {
        when:
            def parameters = this.getParameters()
        then:
            def byName = parameters.collectEntries { [(it.name): it] }
            byName[SystemParameters.GOOGLE_CALENDAR_SYNC].value == "false"
            byName[SystemParameters.GOOGLE_CALENDAR_SYNC].type == "BOOLEAN"
            byName[SystemParameters.GOOGLE_CALENDAR_NAME].value == "Task"
            byName[SystemParameters.GOOGLE_CALENDAR_APPLICATION_NAME].value == "Task"
            byName[SystemParameters.GOOGLE_CALENDAR_CREDENTIALS].type == "SECRET"
            !byName[SystemParameters.GOOGLE_CALENDAR_CREDENTIALS].valueSet
    }

    def "Connection status should be NOT_CONFIGURED when there is no key"() {
        expect:
            this.getConnection().status == GoogleCalendarConnectionStatusDto.NOT_CONFIGURED
    }

    def "Turning sync on without a key should return 400"() {
        when:
            def response = this.updateParameter(SystemParameters.GOOGLE_CALENDAR_SYNC, "true")
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.getParameter(SystemParameters.GOOGLE_CALENDAR_SYNC).value == "false"
    }

    def "Saving text that is not a service account key should return 400"() {
        when:
            def response = this.updateParameter(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS,
                    '{"type": "authorized_user"}')
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            !this.getParameter(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS).valueSet
    }

    def "A saved service account key should never be returned, kept when no value is sent and removed when an empty one is sent"() {
        given:
            def key = prepareServiceAccountKey()
        when:
            def response = this.updateParameter(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS, key)
        then:
            response.status == HttpStatus.OK.value()
            !response.contentAsString.contains("private_key")
            MAPPER.readValue(response.contentAsString, SystemParameterDto).valueSet
        and: "the list and the single parameter do not contain the key"
            def listResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + SYSTEM_PARAMETERS_URI))).andReturn().response
            !listResponse.contentAsString.contains("private_key")
            def credentials = this.getParameter(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS)
            credentials.value == null
            credentials.defaultValue == null
            credentials.valueSet
        and: "sync is off, so Google is not called and the key's account is reported"
            def connection = this.getConnection()
            connection.status == GoogleCalendarConnectionStatusDto.DISABLED
            connection.serviceAccountEmail == SERVICE_ACCOUNT_EMAIL
        when: "no value is sent"
            def keepResponse = this.updateParameter(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS, null)
        then: "the stored key is kept"
            keepResponse.status == HttpStatus.OK.value()
            this.systemParameterQueryService.getValueByName(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS) == key
        when: "an empty value is sent while sync is turned off"
            def removeResponse = this.updateParameter(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS, " ")
        then: "the stored key is removed"
            removeResponse.status == HttpStatus.OK.value()
            this.systemParameterQueryService.getValueByName(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS) == ""
            !this.getParameter(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS).valueSet
            this.getConnection().status == GoogleCalendarConnectionStatusDto.NOT_CONFIGURED
    }

    def "Google Calendar name cannot be blank"() {
        when:
            def response = this.updateParameter(SystemParameters.GOOGLE_CALENDAR_NAME, " ")
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.getParameter(SystemParameters.GOOGLE_CALENDAR_NAME).value == "Task"
    }

    private def updateParameter(String name, String value) {
        def parameter = this.getParameter(name)
        def updateDto = new SystemParameterDto(name: name, value: value)
        return this.mockMvc.perform(MockMvcRequestBuilders
                .put(URI.create(HOST + ":" + port + SYSTEM_PARAMETERS_URI + "/" + parameter.id))
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(updateDto))).andReturn().response
    }

    private List<SystemParameterDto> getParameters() {
        def response = this.mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create(HOST + ":" + port + SYSTEM_PARAMETERS_URI))).andReturn().response
        return MAPPER.readValue(response.contentAsString, new TypeReference<List<SystemParameterDto>>() {})
    }

    private SystemParameterDto getParameter(String name) {
        return this.getParameters().find { it.name == name }
    }

    private GoogleCalendarConnectionDto getConnection() {
        def response = this.mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create(HOST + ":" + port + CONNECTION_STATUS_URI))).andReturn().response
        return MAPPER.readValue(response.contentAsString, GoogleCalendarConnectionDto)
    }

    private static String prepareServiceAccountKey() {
        def keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        def encodedKey = Base64.getMimeEncoder(64, "\n".bytes).encodeToString(
                keyPairGenerator.generateKeyPair().private.encoded)
        def privateKey = "-----BEGIN PRIVATE KEY-----\n${encodedKey}\n-----END PRIVATE KEY-----\n".toString()
        return MAPPER.writeValueAsString([type          : "service_account",
                                          project_id    : "task-test",
                                          private_key_id: "key123",
                                          private_key   : privateKey,
                                          client_email  : SERVICE_ACCOUNT_EMAIL,
                                          client_id     : "123456789",
                                          token_uri     : "https://oauth2.googleapis.com/token"])
    }
}
