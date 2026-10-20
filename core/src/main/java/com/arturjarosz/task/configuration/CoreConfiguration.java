package com.arturjarosz.task.configuration;

import com.arturjarosz.task.properties.GoogleCalendarProperties;
import com.arturjarosz.task.properties.ProjectProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableJpaAuditing
@EnableScheduling
@EnableConfigurationProperties({GoogleCalendarProperties.class, ProjectProperties.class})
public class CoreConfiguration {
}
