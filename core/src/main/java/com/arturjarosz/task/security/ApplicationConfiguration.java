package com.arturjarosz.task.security;

import com.arturjarosz.task.properties.SecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class ApplicationConfiguration {
    private static final String MATCH_ALL = "/**";

    private static final List<String> HEADERS = List.of(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
            HttpHeaders.AUTHORIZATION, HttpHeaders.CACHE_CONTROL, HttpHeaders.CONTENT_TYPE, HttpHeaders.SET_COOKIE);
    private static final List<String> API_METHODS = Arrays.asList(HttpMethod.GET.name(), HttpMethod.POST.name(),
            HttpMethod.PATCH.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name());


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request.requestMatchers(new AntPathRequestMatcher(MATCH_ALL))
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(SecurityProperties securityProperties) {
        LOG.info("Allowed CORS Origin domains: {}", securityProperties.allowedOrigins());
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(new ArrayList<>(securityProperties.allowedOrigins()));
        corsConfiguration.setAllowedHeaders(HEADERS);
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(API_METHODS);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(MATCH_ALL, corsConfiguration);
        return source;
    }
}
