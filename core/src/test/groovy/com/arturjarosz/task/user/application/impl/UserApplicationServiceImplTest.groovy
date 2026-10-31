package com.arturjarosz.task.user.application.impl

import com.arturjarosz.task.dto.UserConfigurationDto
import com.arturjarosz.task.dto.UserConfigurationNameDto
import com.arturjarosz.task.dto.UserDto
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import com.arturjarosz.task.user.application.CurrentUserProvider
import com.arturjarosz.task.user.application.mapper.UserMapper
import com.arturjarosz.task.user.model.User
import com.arturjarosz.task.user.model.UserConfiguration
import com.arturjarosz.task.user.model.UserConfigurationName
import spock.lang.Specification

class UserApplicationServiceImplTest extends Specification {
    final static USERNAME = "testuser"
    final static EMAIL = "test@example.com"
    final static AUTH_ID = "auth0|123456789"
    final static USER_ID = 1L

    def currentUserProvider = Mock(CurrentUserProvider)
    def userMapper = Mock(UserMapper)

    def subject = new UserApplicationServiceImpl(currentUserProvider, userMapper)

    def "getCurrentUser should return current user, creating it when needed"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)
            def userDto = new UserDto(id: USER_ID, username: USERNAME, email: EMAIL, authId: AUTH_ID)
            this.currentUserProvider.findOrCreateCurrentUser() >> Optional.of(user)
            this.userMapper.mapToDto(user) >> userDto

        when:
            def result = this.subject.getCurrentUser()

        then:
            result == userDto
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
            this.currentUserProvider.findOrCreateCurrentUser() >> Optional.empty()
            this.userMapper.mapConfigurationName(UserConfigurationNameDto.THEME_MODE) >> UserConfigurationName.THEME_MODE

        when:
            this.subject.updateCurrentUserConfiguration(UserConfigurationNameDto.THEME_MODE,
                    new UserConfigurationDto(value: "LIGHT"))

        then:
            thrown(ResourceNotFoundException)
    }

    private void mockCurrentUser(User user) {
        this.currentUserProvider.findOrCreateCurrentUser() >> Optional.of(user)
        this.userMapper.mapConfigurationName(UserConfigurationNameDto.THEME_MODE) >> UserConfigurationName.THEME_MODE
    }
}
