package com.arturjarosz.task.properties;

import com.arturjarosz.task.project.status.project.ProjectStatus;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(ProjectProperties.PROJECT_PROPERTY_PATH)
public record ProjectProperties(Set<ProjectStatus> activeStatuses) {
    static final String PROJECT_PROPERTY_PATH = "task.project";

    public ProjectProperties {
        activeStatuses = activeStatuses == null ? Set.of() : Set.copyOf(activeStatuses);
    }
}
