package com.arturjarosz.task.security

import jakarta.servlet.Filter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

@ActiveProfiles("secured")
@WebAppConfiguration
@ContextConfiguration(classes = [SecurityConfiguration, OAuth2Configuration])
@TestPropertySource(properties = "task.security.allowed-origins=http://localhost")
class SecurityConfigurationTest extends Specification {

    @Autowired
    WebApplicationContext context

    MockMvc mockMvc

    def setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .addFilters(this.context.getBean("springSecurityFilterChain", Filter))
                .build()
    }

    def "Logout with POST should log the user out"() {
        given:
            def session = authenticatedSession()
        when:
            this.mockMvc.perform(MockMvcRequestBuilders.post("/logout").session(session))
        then:
            session.invalid
    }

    def "Logout with GET should not log the user out"() {
        given:
            def session = authenticatedSession()
        when:
            this.mockMvc.perform(MockMvcRequestBuilders.get("/logout").session(session))
        then:
            !session.invalid
    }

    private static MockHttpSession authenticatedSession() {
        def session = new MockHttpSession()
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new SecurityContextImpl(UsernamePasswordAuthenticationToken.authenticated("user", null, [])))
        return session
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class OAuth2Configuration {

        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(ClientRegistration.withRegistrationId("okta")
                    .clientId("task")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .authorizationUri("https://issuer.test/authorize")
                    .tokenUri("https://issuer.test/token")
                    .build())
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return { String token -> throw new BadJwtException("Tokens are not decoded in this test") } as JwtDecoder
        }
    }
}
