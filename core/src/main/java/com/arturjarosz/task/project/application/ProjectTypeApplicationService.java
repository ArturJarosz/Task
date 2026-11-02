package com.arturjarosz.task.project.application;

import com.arturjarosz.task.dto.ProjectTypeDto;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryApplicationService;

/**
 * Project types configured by users, each with the number of items using it.
 */
public interface ProjectTypeApplicationService extends NamedEntryApplicationService<ProjectTypeDto> {
}
