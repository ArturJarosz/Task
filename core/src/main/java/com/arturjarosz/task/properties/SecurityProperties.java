package com.arturjarosz.task.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(SecurityProperties.SECURITY_PROPERTY_PATH)
public record SecurityProperties(Set<String> allowedOrigins) {
    static final String SECURITY_PROPERTY_PATH = "task.security";
}
