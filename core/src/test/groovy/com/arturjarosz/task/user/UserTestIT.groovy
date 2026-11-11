package com.arturjarosz.task.user

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.PermissionActionDto
import com.arturjarosz.task.dto.PermissionDto
import com.arturjarosz.task.dto.PermissionResourceDto
import com.arturjarosz.task.dto.UserConfigurationDto
import com.arturjarosz.task.dto.UserConfigurationNameDto
import com.arturjarosz.task.dto.UserDto
import com.arturjarosz.task.dto.UserRolesDto
import com.arturjarosz.task.dto.UserSummaryDto
import com.arturjarosz.task.role.infrastructure.repository.RoleRepository
import com.arturjarosz.task.role.model.Role
import com.arturjarosz.task.security.permission.Action
import com.arturjarosz.task.security.permission.Permission
import com.arturjarosz.task.security.permission.Resource
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.user.application.CurrentUserProvider
import com.arturjarosz.task.user.infrastructure.repository.UserRepository
import com.arturjarosz.task.user.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class UserTestIT extends BaseTestIT {
    static final String CURRENT_USER_URI = "/users/current"
    static final String CONFIGURATIONS_URI = CURRENT_USER_URI + "/configurations"
    static final String THEME_MODE_URI = CONFIGURATIONS_URI + "/THEME_MODE"
    static final String DASHBOARD_LAYOUT_URI = CONFIGURATIONS_URI + "/DASHBOARD_LAYOUT"
    static final String AUTH_ID = "auth0|configuration-test"
    static final String M2M_AUTH_ID = "configuration-test-client@clients"
    static final ObjectMapper MAPPER = new ObjectMapper()

    @Autowired
    MockMvc mockMvc

    @Autowired
    UserRepository userRepository

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    RoleRepository roleRepository

    def cleanup() {
        SecurityContextHolder.clearContext()
        this.userRepository.findByAuthId(AUTH_ID).ifPresent { this.userRepository.delete(it) }
    }

    @Transactional
    def "Getting users should return them sorted by id, without their Auth0 ids"() {
        given:
            def first = this.userRepository.save(new User("Zofia list test", "zofia@example.com", "auth0|list-test-2"))
            def second = this.userRepository.save(new User("anna list test", "anna@example.com", "auth0|list-test-1"))
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(HOST + ":" + port + "/users")))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
            !response.contentAsString.contains("auth0|list-test")
        and:
            List<UserSummaryDto> users = MAPPER.readValue(response.contentAsString,
                    new TypeReference<List<UserSummaryDto>>() {})
            users.findAll { it.username.endsWith("list test") } == [
                    new UserSummaryDto(id: first.id, username: "Zofia list test", email: "zofia@example.com"),
                    new UserSummaryDto(id: second.id, username: "anna list test", email: "anna@example.com")]
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
            this.getConfigurations() == [new UserConfigurationDto(name: UserConfigurationNameDto.THEME_MODE, value: "LIGHT")]
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

    def "Saving dashboard layout longer than 255 characters should store all of it"() {
        given:
            def user = this.userRepository.save(new User("Configuration user", "configuration@example.com", AUTH_ID))
            this.authenticate(AUTH_ID)
            def widgets = (1..20).collect { '{"type":"widget-' + it + '","width":25}' }.join(",")
            def layout = '{"version":1,"rows":[{"widgets":[' + widgets + ']}]}'
        when:
            def response = this.putConfiguration(DASHBOARD_LAYOUT_URI, layout)
        then:
            layout.length() > 255
            response.status == HttpStatus.OK.value()
            this.jdbcTemplate.queryForList("SELECT VALUE FROM USER_CONFIGURATION WHERE USER_ID = ?", String, user.id) ==
                    [layout]
    }

    def "Saving dashboard layout longer than 4000 characters should return 400 and store nothing"() {
        given:
            def user = this.userRepository.save(new User("Configuration user", "configuration@example.com", AUTH_ID))
            this.authenticate(AUTH_ID)
        when:
            def response = this.putConfiguration(DASHBOARD_LAYOUT_URI, "x" * 4001)
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

    def "Saving theme mode when current user does not exist should create the user and store the configuration"() {
        given:
            this.authenticate(AUTH_ID)
        when:
            def response = this.putThemeMode("LIGHT")
        then:
            response.status == HttpStatus.OK.value()
            this.getConfigurations() == [new UserConfigurationDto(name: UserConfigurationNameDto.THEME_MODE, value: "LIGHT")]
    }

    def "Getting current user on first login should create the user from token claims"() {
        given:
            this.authenticateWithProfile(AUTH_ID, "new@example.com", "New user")
        when:
            def user = this.getCurrentUser()
        then:
            user.id != null
            user.authId == AUTH_ID
            user.username == "New user"
            user.email == "new@example.com"
        and:
            this.jdbcTemplate.queryForList("SELECT USERNAME, EMAIL FROM LOCAL_USER WHERE AUTH_ID = ?", AUTH_ID) ==
                    [[username: "New user", email: "new@example.com"]]
    }

    def "Getting current user with machine-to-machine token should not create a user"() {
        given:
            this.authenticate(M2M_AUTH_ID, ["gty": "client-credentials"])
        when:
            def user = this.getCurrentUser()
        then:
            user.id == null
            this.userRepository.findByAuthId(M2M_AUTH_ID).isEmpty()
    }

    def "Getting current user again should keep one user and refresh it from token claims"() {
        given:
            this.authenticateWithProfile(AUTH_ID, "new@example.com", "New user")
            def createdUser = this.getCurrentUser()
            this.authenticateWithProfile(AUTH_ID, "changed@example.com", "Changed")
        when:
            def user = this.getCurrentUser()
        then:
            user.id == createdUser.id
            user.username == "Changed"
            user.email == "changed@example.com"
            this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM LOCAL_USER WHERE AUTH_ID = ?", Integer, AUTH_ID) == 1
    }

    def "Concurrent first requests of the same user should all succeed and create one user"() {
        given:
            def requests = 8
            def executor = Executors.newFixedThreadPool(requests)
            // Releases all requests at once, so they race for creating the user
            def allReady = new CyclicBarrier(requests)
            def tasks = (1..requests).collect {
                { ->
                    try {
                        allReady.await()
                        authenticateWithProfile(AUTH_ID, "new@example.com", null)
                        return this.getCurrentUserResponse().status
                    } finally {
                        SecurityContextHolder.clearContext()
                    }
                } as Callable<Integer>
            }
        when:
            // One deadline for all requests; a cancelled (timed out) one fails the test in get()
            def statuses = executor.invokeAll(tasks, 30, TimeUnit.SECONDS)*.get()
        then:
            statuses == [HttpStatus.OK.value()] * requests
            this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM LOCAL_USER WHERE AUTH_ID = ?", Integer, AUTH_ID) == 1
        cleanup:
            executor.shutdownNow()
    }

    def "Configurations of current user who does not exist should be empty"() {
        given:
            this.authenticate(AUTH_ID)
        expect:
            this.getConfigurations().isEmpty()
            this.userRepository.findByAuthId(AUTH_ID).isEmpty()
    }

    @Transactional
    def "Updating roles of user should replace them and return the user with new roles"() {
        given:
            def user = new User("Roles user", "roles@example.com", AUTH_ID)
            user.assignRoles([this.roleId("Administrator")])
            user = this.userRepository.saveAndFlush(user)
            def roleIds = [this.roleId("Architect"), this.roleId("Read only")]
        when:
            def response = this.putUserRoles(user.id, new UserRolesDto(roleIds: roleIds))
        then:
            response.status == HttpStatus.OK.value()
            (MAPPER.readValue(response.contentAsString, UserSummaryDto).roleIds as Set) == roleIds as Set
        and:
            this.jdbcTemplate.queryForList("SELECT ROLE_ID FROM USER_ROLE WHERE USER_ID = ?", Long, user.id) as Set ==
                    roleIds as Set
        and:
            def users = MAPPER.readValue(this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + "/users"))).andReturn().response.contentAsString,
                    new TypeReference<List<UserSummaryDto>>() {})
            (users.find { it.id == user.id }.roleIds as Set) == roleIds as Set
    }

    // not transactional, so the roles are saved and taken away in separate sessions, like in two requests
    def "Updating roles of user with empty list should take all roles away"() {
        given:
            def user = new User("Roles user", "roles@example.com", AUTH_ID)
            user.assignRoles([this.roleId("Architect")])
            user = this.userRepository.saveAndFlush(user)
        when:
            def response = this.putUserRoles(user.id, new UserRolesDto(roleIds: []))
        then:
            response.status == HttpStatus.OK.value()
            this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USER_ROLE WHERE USER_ID = ?", Integer, user.id) == 0
    }

    @Transactional
    def "Updating roles of user with not existing role should return 404 and keep the roles"() {
        given:
            def user = new User("Roles user", "roles@example.com", AUTH_ID)
            user.assignRoles([this.roleId("Architect")])
            user = this.userRepository.saveAndFlush(user)
        when:
            def response = this.putUserRoles(user.id, new UserRolesDto(roleIds: [this.roleId("Read only"), 10000L]))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            MAPPER.readValue(response.contentAsString, ErrorMessage).message == "Role with id 10,000 does not exist."
            this.jdbcTemplate.queryForList("SELECT ROLE_ID FROM USER_ROLE WHERE USER_ID = ?", Long, user.id) ==
                    [this.roleId("Architect")]
    }

    @Transactional
    def "Updating roles of not existing user should return 404"() {
        when:
            def response = this.putUserRoles(10000L, new UserRolesDto(roleIds: [this.roleId("Architect")]))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            MAPPER.readValue(response.contentAsString, ErrorMessage).message == "User with id 10,000 does not exist."
    }

    @Transactional
    def "Updating roles of user without role list should return 400"() {
        given:
            def user = this.userRepository.saveAndFlush(new User("Roles user", "roles@example.com", AUTH_ID))
        when:
            def response = this.putUserRoles(user.id, new UserRolesDto(roleIds: null))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            MAPPER.readValue(response.contentAsString, ErrorMessage).message == "Roles of the user have to be provided."
    }

    @Transactional
    def "Current user should have the permissions of all their roles"() {
        given:
            def user = new User("Roles user", "roles@example.com", AUTH_ID)
            user.assignRoles(roleNames.collect { this.roleId(it) })
            this.userRepository.saveAndFlush(user)
            this.authenticate(AUTH_ID)
        when:
            def currentUser = this.getCurrentUser()
        then:
            (currentUser.roleIds as Set) == roleNames.collect { this.roleId(it) } as Set
            currentUser.permissions == permissions(roleNames)
        where:
            roleNames << [[], ["Read only"], ["Read only", "Architect"], ["Administrator"]]
    }

    // Administrator is predefined; the other roles are created on first use and kept, like the BaseTestIT fixtures
    private static final Map<String, List<Permission>> TEST_ROLES = [
            "Architect": [Resource.CLIENT, Resource.PROJECT, Resource.TIME_ENTRY, Resource.CALENDAR]
                    .collectMany { resource -> resource.actions.collect { new Permission(resource, it) } },
            "Read only": [Resource.PROJECT, Resource.CLIENT, Resource.ARCHITECT, Resource.CALENDAR]
                    .collect { new Permission(it, Action.READ) }]

    // All permissions of the roles with given names, as the current user gets them: in order, without repeats
    private static List<PermissionDto> permissions(List<String> roleNames) {
        def permissions = roleNames.contains("Administrator") ? Permission.all()
                : roleNames.collectMany { TEST_ROLES[it] }.toSet()
        return permissions.toSorted(Permission.ORDER).collect {
            new PermissionDto(resource: PermissionResourceDto.valueOf(it.resource().name()),
                    action: PermissionActionDto.valueOf(it.action().name()))
        }
    }

    private Long roleId(String name) {
        return (this.roleRepository.findAll().find { it.name == name }
                ?: this.roleRepository.saveAndFlush(new Role(name, TEST_ROLES[name]))).id
    }

    private def putUserRoles(Long userId, UserRolesDto userRolesDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put(URI.create(HOST + ":" + port + "/users/$userId/roles"))
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(userRolesDto))).andReturn().response
    }

    private def putThemeMode(String value) {
        return this.putConfiguration(THEME_MODE_URI, value)
    }

    private def putConfiguration(String uri, String value) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put(URI.create(HOST + ":" + port + uri))
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(new UserConfigurationDto(value: value)))).andReturn().response
    }

    private def getCurrentUserResponse() {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(HOST + ":" + port + CURRENT_USER_URI)))
                .andReturn().response
    }

    private UserDto getCurrentUser() {
        return MAPPER.readValue(this.getCurrentUserResponse().contentAsString, UserDto)
    }

    private List<UserConfigurationDto> getConfigurations() {
        def response = this.mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create(HOST + ":" + port + CONFIGURATIONS_URI))).andReturn().response
        return MAPPER.readValue(response.contentAsString, new TypeReference<List<UserConfigurationDto>>() {})
    }

    // Claims the Auth0 Action adds; a null value leaves the claim out
    private static void authenticateWithProfile(String authId, String email, String name) {
        authenticate(authId, [(CurrentUserProvider.EMAIL_CLAIM): email, (CurrentUserProvider.NAME_CLAIM): name]
                .findAll { it.value != null })
    }
}
