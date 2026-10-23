package com.arturjarosz.task.user

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.UserConfigurationDto
import com.arturjarosz.task.dto.UserConfigurationNameDto
import com.arturjarosz.task.user.infrastructure.repository.UserRepository
import com.arturjarosz.task.user.model.User
import com.arturjarosz.task.user.model.UserConfigurationName
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

class UserTestIT extends BaseTestIT {
    static final String CONFIGURATIONS_URI = "/users/current/configurations"
    static final String THEME_MODE_URI = CONFIGURATIONS_URI + "/THEME_MODE"
    static final String AUTH_ID = "auth0|configuration-test"
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
    }

    def "Saving theme mode should store it as user configuration and return it with configurations of current user"() {
        given:
            def user = this.userRepository.save(new User("Configuration user", "configuration@example.com", AUTH_ID))
            this.authenticate(AUTH_ID)
        when:
            def response = this.putThemeMode("DARK")
        then:
            response.status == HttpStatus.OK.value()
            MAPPER.readValue(response.contentAsString, UserConfigurationDto) ==
                    new UserConfigurationDto(name: UserConfigurationNameDto.THEME_MODE, value: "DARK")
        and:
            this.jdbcTemplate.queryForList("SELECT NAME, VALUE FROM USER_CONFIGURATION WHERE USER_ID = ?", user.id) ==
                    [[name: "THEME_MODE", value: "DARK"]]
        and:
            this.getConfigurations() == [new UserConfigurationDto(name: UserConfigurationNameDto.THEME_MODE, value: "DARK")]
    }

    def "Saving theme mode again should update the stored configuration instead of adding another one"() {
        given:
            def user = this.userRepository.save(new User("Configuration user", "configuration@example.com", AUTH_ID))
            this.authenticate(AUTH_ID)
            this.putThemeMode("DARK")
        when:
            def response = this.putThemeMode("LIGHT")
        then:
            response.status == HttpStatus.OK.value()
            this.jdbcTemplate.queryForList("SELECT VALUE FROM USER_CONFIGURATION WHERE USER_ID = ?", String, user.id) ==
                    ["LIGHT"]
            this.userRepository.findByAuthId(AUTH_ID).get()
                    .getConfiguration(UserConfigurationName.THEME_MODE).get().value == "LIGHT"
    }

    def "Saving theme mode with value that is not a theme mode should return 400 and store nothing"() {
        given:
            def user = this.userRepository.save(new User("Configuration user", "configuration@example.com", AUTH_ID))
            this.authenticate(AUTH_ID)
        when:
            def response = this.putThemeMode("BLUE")
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USER_CONFIGURATION WHERE USER_ID = ?", Integer,
                    user.id) == 0
    }

    def "Saving configuration with unknown name should return 400"() {
        given:
            this.userRepository.save(new User("Configuration user", "configuration@example.com", AUTH_ID))
            this.authenticate(AUTH_ID)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + CONFIGURATIONS_URI + "/FONT_SIZE"))
                    .header("Content-Type", "application/json")
                    .content(MAPPER.writeValueAsString(new UserConfigurationDto(value: "LARGE")))).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
    }

    def "Saving theme mode when current user does not exist should return 404"() {
        given:
            this.authenticate("auth0|not-registered")
        when:
            def response = this.putThemeMode("LIGHT")
        then:
            response.status == HttpStatus.NOT_FOUND.value()
    }

    def "Configurations of current user who does not exist should be empty"() {
        given:
            this.authenticate("auth0|not-registered")
        expect:
            this.getConfigurations().isEmpty()
    }

    private def putThemeMode(String value) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put(URI.create(HOST + ":" + port + THEME_MODE_URI))
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(new UserConfigurationDto(value: value)))).andReturn().response
    }

    private List<UserConfigurationDto> getConfigurations() {
        def response = this.mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create(HOST + ":" + port + CONFIGURATIONS_URI))).andReturn().response
        return MAPPER.readValue(response.contentAsString, new TypeReference<List<UserConfigurationDto>>() {})
    }

    // The test profile runs without the security filter chain, so the JWT goes straight into the context
    private static void authenticate(String authId) {
        def jwt = new Jwt("token", null, null, ["alg": "RS256"], ["sub": authId])
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt))
    }
}
