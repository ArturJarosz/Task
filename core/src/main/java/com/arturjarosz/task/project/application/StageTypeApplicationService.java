package com.arturjarosz.task.project.application;

import com.arturjarosz.task.dto.StageTypeDto;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryApplicationService;

/**
 * Stage types configured by users, each with the number of items using it.
 */
public interface StageTypeApplicationService extends NamedEntryApplicationService<StageTypeDto> {
}
