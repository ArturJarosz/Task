package com.arturjarosz.task.project.application;

import com.arturjarosz.task.dto.TaskTypeDto;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryApplicationService;

/**
 * Task types configured by users, each with the number of items using it.
 */
public interface TaskTypeApplicationService extends NamedEntryApplicationService<TaskTypeDto> {
}
