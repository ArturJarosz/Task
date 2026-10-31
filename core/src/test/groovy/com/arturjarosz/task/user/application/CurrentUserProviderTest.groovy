package com.arturjarosz.task.user.application

import com.arturjarosz.task.user.infrastructure.repository.UserRepository
import com.arturjarosz.task.user.model.User
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import spock.lang.Specification

class CurrentUserProviderTest extends Specification {
    final static USERNAME = "testuser"
    final static EMAIL = "test@example.com"
    final static AUTH_ID = "auth0|123456789"
    final static PROFILE_CLAIMS = [(CurrentUserProvider.EMAIL_CLAIM): EMAIL, (CurrentUserProvider.NAME_CLAIM): USERNAME]

    def userRepository = Mock(UserRepository)
    def securityContext = Mock(SecurityContext)

    def subject = new CurrentUserProvider(userRepository)

    def setup() {
        SecurityContextHolder.setContext(securityContext)
    }

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def "findCurrentUser should return user matching sub claim without creating it"() {
        given:
            def user = new User(USERNAME, EMAIL, AUTH_ID)
            this.authenticate(createJwt(AUTH_ID))
            this.userRepository.findByAuthId(AUTH_ID) >> Optional.of(user)

        when:
            def result = this.subject.findCurrentUser()

        then:
            result.get() == user
            0 * this.userRepository.save(_)
    }

    def "findCurrentUser should return empty optional when user does not exist"() {
        given:
            this.authenticate(createJwt(AUTH_ID))
            this.userRepository.findByAuthId(AUTH_ID) >> Optional.empty()

        expect:
            this.subject.findCurrentUser().isEmpty()
    }

    def "findOrCreateCurrentUser should return empty optional without looking up user when token does not belong to a person"() {
        given:
            this.securityContext.getAuthentication() >> authentication

        when:
            def result = this.subject.findOrCreateCurrentUser()

        then:
            result.isEmpty()
            0 * this.userRepository.findByAuthId(_)
            0 * this.userRepository.save(_)

        where:
            authentication << [
                    null,
                    Mock(Authentication),
                    new JwtAuthenticationToken(new Jwt("token", null, null, ["alg": "RS256"], ["iss": "test-issuer"])),
                    new JwtAuthenticationToken(createJwt("client-id@clients", ["gty": "client-credentials"]))
            ]
    }

    def "findOrCreateCurrentUser should lock authId and create user from JWT claims when user does not exist"() {
        given:
            this.authenticate(createJwt(AUTH_ID, PROFILE_CLAIMS))
            this.userRepository.findByAuthId(AUTH_ID) >> Optional.empty()

        when:
            def result = this.subject.findOrCreateCurrentUser()

        then:
            1 * this.userRepository.lockAuthId(AUTH_ID)
        then:
            1 * this.userRepository.save({ User u ->
                u.authId == AUTH_ID && u.username == USERNAME && u.email == EMAIL
            }) >> { User u -> u }
        and:
            result.get().authId == AUTH_ID
    }

    def "findOrCreateCurrentUser should create user with empty profile when JWT has no profile claims"() {
        given:
            this.authenticate(createJwt(AUTH_ID))
            this.userRepository.findByAuthId(AUTH_ID) >> Optional.empty()

        when:
            this.subject.findOrCreateCurrentUser()

        then:
            1 * this.userRepository.save({ User u -> u.authId == AUTH_ID && u.username == "" && u.email == "" }) >>
                    { User u -> u }
    }

    def "findOrCreateCurrentUser should return user created by concurrent request while waiting for the lock"() {
        given:
            def concurrentlyCreatedUser = new User(USERNAME, EMAIL, AUTH_ID)
            this.authenticate(createJwt(AUTH_ID))
            this.userRepository.findByAuthId(AUTH_ID) >>> [Optional.empty(), Optional.of(concurrentlyCreatedUser)]

        when:
            def result = this.subject.findOrCreateCurrentUser()

        then:
            1 * this.userRepository.lockAuthId(AUTH_ID)
            0 * this.userRepository.save(_)
            result.get() == concurrentlyCreatedUser
    }

    def "findOrCreateCurrentUser should refresh profile of existing user from JWT claims without locking"() {
        given:
            def user = new User("old name", "old@example.com", AUTH_ID)
            this.authenticate(createJwt(AUTH_ID, PROFILE_CLAIMS))
            this.userRepository.findByAuthId(AUTH_ID) >> Optional.of(user)

        when:
            this.subject.findOrCreateCurrentUser()

        then:
            user.username == USERNAME
            user.email == EMAIL
            0 * this.userRepository.lockAuthId(_)
            0 * this.userRepository.save(_)
    }

    private void authenticate(Jwt jwt) {
        this.securityContext.getAuthentication() >> new JwtAuthenticationToken(jwt)
    }

    private static Jwt createJwt(String subClaim, Map<String, Object> otherClaims = [:]) {
        return new Jwt("token", null, null, ["alg": "RS256", "typ": "JWT"], ["sub": subClaim] + otherClaims)
    }
}
