package com.arturjarosz.task.user.application.impl

import com.arturjarosz.task.dto.UserDto
import com.arturjarosz.task.user.application.mapper.UserMapper
import com.arturjarosz.task.user.infrastructure.repository.UserRepository
import com.arturjarosz.task.user.model.User
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import spock.lang.Specification

class UserApplicationServiceImplTest extends Specification {
    final static USERNAME = "testuser"
    final static EMAIL = "test@example.com"
    final static AUTH_ID = "auth0|123456789"
    final static USER_ID = 1L

    def userRepository = Mock(UserRepository)
    def userMapper = Mock(UserMapper)
    def securityContext = Mock(SecurityContext)

    def subject = new UserApplicationServiceImpl(userRepository, userMapper)

    def setup() {
        SecurityContextHolder.setContext(securityContext)
    }

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def "getCurrentUser should return user when JWT authentication with valid sub claim and user exists"() {
        given:
            def jwt = createJwt(AUTH_ID)
            def jwtAuth = new JwtAuthenticationToken(jwt)
            def user = new User(USERNAME, EMAIL, AUTH_ID)
            def userDto = new UserDto(id: USER_ID, username: USERNAME, email: EMAIL, authId: AUTH_ID)

            this.securityContext.getAuthentication() >> jwtAuth
            this.userRepository.findByAuthId(AUTH_ID) >> Optional.of(user)
            this.userMapper.mapToDto(user) >> userDto

        when:
            def result = this.subject.getCurrentUser()

        then:
            result != null
            result.id == USER_ID
            result.username == USERNAME
            result.email == EMAIL
            result.authId == AUTH_ID
    }

    def "getCurrentUser should return empty UserDto when JWT authentication with valid sub claim but user does not exist"() {
        given:
            def jwt = createJwt(AUTH_ID)
            def jwtAuth = new JwtAuthenticationToken(jwt)

            this.securityContext.getAuthentication() >> jwtAuth
            this.userRepository.findByAuthId(AUTH_ID) >> Optional.empty()

        when:
            def result = this.subject.getCurrentUser()

        then:
            result != null
            result.id == null
            result.username == null
            result.email == null
    }

    def "getCurrentUser should return empty UserDto when JWT authentication with null sub claim"() {
        given:
            def jwt = createJwtWithoutSub()
            def jwtAuth = new JwtAuthenticationToken(jwt)

            this.securityContext.getAuthentication() >> jwtAuth

        when:
            def result = this.subject.getCurrentUser()

        then:
            result != null
            result.id == null
            0 * this.userRepository.findByAuthId(_)
    }

    def "getCurrentUser should return empty UserDto when authentication is not JwtAuthenticationToken"() {
        given:
            def authentication = Mock(Authentication)
            this.securityContext.getAuthentication() >> authentication

        when:
            def result = this.subject.getCurrentUser()

        then:
            result != null
            result.id == null
            0 * this.userRepository.findByAuthId(_)
    }

    def "getCurrentUser should return empty UserDto when authentication is null"() {
        given:
            this.securityContext.getAuthentication() >> null

        when:
            def result = this.subject.getCurrentUser()

        then:
            result != null
            result.id == null
            0 * this.userRepository.findByAuthId(_)
    }

    def "createUser should set authId from JWT token and save user"() {
        given:
            def userDto = new UserDto(username: USERNAME, email: EMAIL)
            def user = new User(USERNAME, EMAIL, null)
            def savedUserDto = new UserDto(id: USER_ID, username: USERNAME, email: EMAIL, authId: AUTH_ID)

            def jwt = createJwt(AUTH_ID)
            def jwtAuth = new JwtAuthenticationToken(jwt)

            this.securityContext.getAuthentication() >> jwtAuth
            this.userMapper.mapFromDto(userDto) >> user

        when:
            def result = this.subject.createUser(userDto)

        then:
            1 * this.userRepository.save({ User u ->
                u.authId == AUTH_ID
            }) >> user
            1 * this.userMapper.mapToDto(user) >> savedUserDto
        and:
            result != null
            result.id == USER_ID
            result.username == USERNAME
            result.email == EMAIL
            result.authId == AUTH_ID
    }

    def "createUser should save user without authId when authentication is not JwtAuthenticationToken"() {
        given:
            def userDto = new UserDto(username: USERNAME, email: EMAIL)
            def user = new User(USERNAME, EMAIL, null)
            def savedUserDto = new UserDto(id: USER_ID, username: USERNAME, email: EMAIL)

            def authentication = Mock(Authentication)
            this.securityContext.getAuthentication() >> authentication
            this.userMapper.mapFromDto(userDto) >> user

        when:
            def result = this.subject.createUser(userDto)

        then:
            1 * this.userRepository.save(_ as User) >> user
            1 * this.userMapper.mapToDto(user) >> savedUserDto
        and:
            result != null
            result.id == USER_ID
            result.username == USERNAME
            result.email == EMAIL
    }

    def "createUser should save user without authId when JWT token has null sub claim"() {
        given:
            def userDto = new UserDto(username: USERNAME, email: EMAIL)
            def user = new User(USERNAME, EMAIL, null)
            def savedUserDto = new UserDto(id: USER_ID, username: USERNAME, email: EMAIL)

            def jwt = createJwtWithoutSub()
            def jwtAuth = new JwtAuthenticationToken(jwt)

            this.securityContext.getAuthentication() >> jwtAuth
            this.userMapper.mapFromDto(userDto) >> user

        when:
            def result = this.subject.createUser(userDto)

        then:
            1 * this.userRepository.save(_ as User) >> user
            1 * this.userMapper.mapToDto(user) >> savedUserDto
        and:
            result != null
            result.id == USER_ID
            result.username == USERNAME
            result.email == EMAIL
    }

    def "createUser should save user without authId when authentication is null"() {
        given:
            def userDto = new UserDto(username: USERNAME, email: EMAIL)
            def user = new User(USERNAME, EMAIL, null)
            def savedUserDto = new UserDto(id: USER_ID, username: USERNAME, email: EMAIL)

            this.securityContext.getAuthentication() >> null
            this.userMapper.mapFromDto(userDto) >> user

        when:
            def result = this.subject.createUser(userDto)

        then:
            1 * this.userRepository.save(_ as User) >> user
            1 * this.userMapper.mapToDto(user) >> savedUserDto
        and:
            result != null
            result.id == USER_ID
            result.username == USERNAME
            result.email == EMAIL
    }

    private static Jwt createJwt(String subClaim) {
        def headers = ["alg": "RS256", "typ": "JWT"]
        def claims = ["sub": subClaim]
        return new Jwt("token", null, null, headers, claims)
    }

    private static Jwt createJwtWithoutSub() {
        def headers = ["alg": "RS256", "typ": "JWT"]
        def claims = ["iss": "test-issuer"]
        return new Jwt("token", null, null, headers, claims)
    }
}
