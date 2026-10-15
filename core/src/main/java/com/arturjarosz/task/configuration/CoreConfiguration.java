package com.arturjarosz.task.configuration;

import com.arturjarosz.task.properties.GoogleCalendarProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableJpaAuditing
@EnableScheduling
@EnableConfigurationProperties(GoogleCalendarProperties.class)
public class CoreConfiguration {
}
