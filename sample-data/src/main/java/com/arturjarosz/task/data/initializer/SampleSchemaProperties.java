package com.arturjarosz.task.data.initializer;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The database schema of the application's tables, for sample data changed directly in the database.
 */
@ConfigurationProperties(SampleSchemaProperties.HIBERNATE_PROPERTY_PATH)
public record SampleSchemaProperties(String defaultSchema) {
    static final String HIBERNATE_PROPERTY_PATH = "spring.jpa.properties.hibernate";
}
