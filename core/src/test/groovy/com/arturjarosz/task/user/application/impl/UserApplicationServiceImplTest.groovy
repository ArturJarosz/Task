package com.arturjarosz.task.user.application.impl

import com.arturjarosz.task.dto.PermissionActionDto
import com.arturjarosz.task.dto.PermissionDto
import com.arturjarosz.task.dto.PermissionResourceDto
import com.arturjarosz.task.dto.UserConfigurationDto
import com.arturjarosz.task.dto.UserConfigurationNameDto
import com.arturjarosz.task.dto.UserDto
import com.arturjarosz.task.dto.UserRolesDto
import com.arturjarosz.task.dto.UserSummaryDto
import com.arturjarosz.task.role.application.RoleValidator
import com.arturjarosz.task.role.application.mapper.PermissionMapper
import com.arturjarosz.task.security.permission.Action
import com.arturjarosz.task.security.permission.AuthenticatedPrincipal
import com.arturjarosz.task.security.permission.Permission
import com.arturjarosz.task.security.permission.PermissionProvider
import com.arturjarosz.task.security.permission.Resource
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import com.arturjarosz.task.user.application.CurrentUserProvider
import com.arturjarosz.task.user.application.mapper.UserMapper
import com.arturjarosz.task.user.infrastructure.repository.UserRepository
import com.arturjarosz.task.user.model.User
import com.arturjarosz.task.user.model.UserConfiguration
import com.arturjarosz.task.user.model.UserConfigurationName
import org.springframework.data.domain.Sort
import spock.lang.Specification

class UserApplicationServiceImplTest extends Specification {
    final static USERNAME = "testuser"
    final static EMAIL = "test@example.com"
    final static AUTH_ID = "auth0|123456789"
    final static USER_ID = 1L

    def currentUserProvider = Mock(CurrentUserProvider)
    def userMapper = Mock(UserMapper)

    def userRepository = Mock(UserRepository)
    def permissionProvider = Mock(PermissionProvider)
    def roleValidator = Mock(RoleValidator)
    // a real one, as it only maps names
    def permissionMapper = new PermissionMapper() {}

    def subject = new UserApplicationServiceImpl(currentUserProvider, userMapper, userRepository, permissionProvider,
            roleValidator, permissionMapper)

    def "getCurrentUser should return current user, creating it when needed"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)
            def userDto = new UserDto(id: USER_ID, username: USERNAME, email: EMAIL, authId: AUTH_ID)
            this.currentUserProvider.findOrCreateCurrentUser() >> Optional.of(user)
            this.currentUserProvider.findCurrentPrincipal() >> Optional.empty()
            this.userMapper.mapToDto(user) >> userDto

        when:
            def result = this.subject.getCurrentUser()

        then:
            result == userDto
    }

    def "getCurrentUser should return permissions the permission provider gives the current principal"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)
            def principal = new AuthenticatedPrincipal(AUTH_ID, [sub: AUTH_ID])
            def permissions = [new Permission(Resource.PROJECT, Action.READ),
                               new Permission(Resource.PROJECT, Action.EDIT)] as Set
            this.currentUserProvider.findOrCreateCurrentUser() >> Optional.of(user)
            this.currentUserProvider.findCurrentPrincipal() >> Optional.of(principal)
            this.userMapper.mapToDto(user) >> new UserDto(id: USER_ID)
            this.permissionProvider.getPermissions(principal) >> permissions

        when:
            def result = this.subject.getCurrentUser()

        then:
            result.permissions == [
                    new PermissionDto(resource: PermissionResourceDto.PROJECT, action: PermissionActionDto.READ),
                    new PermissionDto(resource: PermissionResourceDto.PROJECT, action: PermissionActionDto.EDIT)]
    }

    def "getCurrentUser should return empty UserDto when token does not belong to a person"() {
        given:
            this.currentUserProvider.findOrCreateCurrentUser() >> Optional.empty()

        when:
            def result = this.subject.getCurrentUser()

        then:
            result != null
            result.id == null
            result.username == null
            result.email == null
    }

    def "getUsers should ask for users sorted by id and return them in that order"() {
        given:
            this.userRepository.findAll(Sort.by("id")) >> [new User("zoe", "zoe@example.com", "auth0|1"),
                                                           new User("Adam", "adam@example.com", "auth0|2")]
            this.userMapper.mapToSummaryDto(_ as User) >> { User user -> new UserSummaryDto(username: user.username) }

        when:
            def result = this.subject.getUsers()

        then:
            result*.username == ["zoe", "Adam"]
    }

    def "getUserSummaries should return found users by id"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)
            def userSummaryDto = new UserSummaryDto(id: USER_ID, username: USERNAME, email: EMAIL)
            this.userRepository.findAllById([USER_ID, 2L]) >> [user]
            this.userMapper.mapToSummaryDto(user) >> userSummaryDto

        when:
            def result = this.subject.getUserSummaries([USER_ID, 2L])

        then:
            result == [(USER_ID): userSummaryDto]
    }

    def "getCurrentUserConfigurations should return configurations of current user"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)
            user.setConfiguration(UserConfigurationName.THEME_MODE, "DARK")
            this.currentUserProvider.findCurrentUser() >> Optional.of(user)
            this.userMapper.mapToDto(_ as UserConfiguration) >> { UserConfiguration configuration ->
                new UserConfigurationDto(name: UserConfigurationNameDto.valueOf(configuration.name.name()),
                        value: configuration.value)
            }

        when:
            def result = this.subject.getCurrentUserConfigurations()

        then:
            result == [new UserConfigurationDto(name: UserConfigurationNameDto.THEME_MODE, value: "DARK")]
    }

    def "getCurrentUserConfigurations should return empty list without creating user when current user does not exist"() {
        given:
            this.currentUserProvider.findCurrentUser() >> Optional.empty()

        when:
            def result = this.subject.getCurrentUserConfigurations()

        then:
            result.isEmpty()
            0 * this.currentUserProvider.findOrCreateCurrentUser()
    }

    def "updateCurrentUserConfiguration should create configuration the user has not defined yet"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)
            mockCurrentUser(user)

        when:
            this.subject.updateCurrentUserConfiguration(UserConfigurationNameDto.THEME_MODE,
                    new UserConfigurationDto(value: "DARK"))

        then:
            user.configurations.size() == 1
            user.getConfiguration(UserConfigurationName.THEME_MODE).get().value == "DARK"
    }

    def "updateCurrentUserConfiguration should update existing configuration instead of adding another one"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)
            user.setConfiguration(UserConfigurationName.THEME_MODE, "DARK")
            mockCurrentUser(user)

        when:
            this.subject.updateCurrentUserConfiguration(UserConfigurationNameDto.THEME_MODE,
                    new UserConfigurationDto(value: "LIGHT"))

        then:
            user.configurations.size() == 1
            user.getConfiguration(UserConfigurationName.THEME_MODE).get().value == "LIGHT"
    }

    def "updateCurrentUserConfiguration should throw exception when value is not valid for configuration"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)
            mockCurrentUser(user)

        when:
            this.subject.updateCurrentUserConfiguration(UserConfigurationNameDto.THEME_MODE,
                    new UserConfigurationDto(value: value))

        then:
            thrown(IllegalArgumentException)
            user.configurations.isEmpty()

        where:
            value << [null, "", "dark", "BLUE"]
    }

    def "updateCurrentUserConfiguration should throw exception when token does not belong to a person"() {
        given:
            this.currentUserProvider.getOrCreateCurrentUser() >> { throw new ResourceNotFoundException("notExist.user") }
            this.userMapper.mapConfigurationName(UserConfigurationNameDto.THEME_MODE) >> UserConfigurationName.THEME_MODE

        when:
            this.subject.updateCurrentUserConfiguration(UserConfigurationNameDto.THEME_MODE,
                    new UserConfigurationDto(value: "LIGHT"))

        then:
            thrown(ResourceNotFoundException)
    }

    def "updateUserRoles should replace roles of the user after checking that each role exists"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)
            user.assignRoles([1L])
            def summary = new UserSummaryDto(id: USER_ID)
            this.userRepository.findById(USER_ID) >> Optional.of(user)
            this.userRepository.saveAndFlush(user) >> user
            this.userMapper.mapToSummaryDto(user) >> summary

        when:
            def result = this.subject.updateUserRoles(USER_ID, new UserRolesDto(roleIds: [2L, 3L]))

        then:
            1 * this.roleValidator.validateExistence(2L)
            1 * this.roleValidator.validateExistence(3L)
            result == summary
            user.roleIds == [2L, 3L] as Set
    }

    def "updateUserRoles should throw exception without changing any user when roles are not given"() {
        when:
            this.subject.updateUserRoles(USER_ID, userRolesDto)

        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "isNull.user.roles"
            0 * this.userRepository.saveAndFlush(_)

        where:
            userRolesDto << [null, new UserRolesDto(roleIds: null), new UserRolesDto(roleIds: [1L, null])]
    }

    def "updateUserRoles should throw ResourceNotFoundException when user does not exist"() {
        given:
            this.userRepository.findById(USER_ID) >> Optional.empty()

        when:
            this.subject.updateUserRoles(USER_ID, new UserRolesDto(roleIds: [1L]))

        then:
            def exception = thrown(ResourceNotFoundException)
            exception.message == "notExist.user.id"
            0 * this.userRepository.saveAndFlush(_)
    }

    private void mockCurrentUser(User user) {
        this.currentUserProvider.getOrCreateCurrentUser() >> user
        this.userMapper.mapConfigurationName(UserConfigurationNameDto.THEME_MODE) >> UserConfigurationName.THEME_MODE
    }
}
