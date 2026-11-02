package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.infrastructure.repositor.StageTypeRepository;
import com.arturjarosz.task.project.model.StageType;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryValidator;
import org.springframework.stereotype.Component;

/**
 * Validates stage type dtos and StageType entities.
 */
@Component
public class StageTypeValidator extends NamedEntryValidator<StageType> {

    public StageTypeValidator(StageTypeRepository stageTypeRepository, ProjectQueryService projectQueryService) {
        super(stageTypeRepository, ProjectExceptionCodes.STAGE_TYPE, ProjectExceptionCodes.STAGES, projectQueryService::getNumberOfStagesByStageTypeIds);
    }
}
