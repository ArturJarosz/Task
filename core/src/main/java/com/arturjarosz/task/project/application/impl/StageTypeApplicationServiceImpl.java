package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.dto.StageTypeDto;
import com.arturjarosz.task.project.application.StageTypeApplicationService;
import com.arturjarosz.task.project.application.StageTypeValidator;
import com.arturjarosz.task.project.application.mapper.StageTypeMapper;
import com.arturjarosz.task.project.infrastructure.repositor.StageTypeRepository;
import com.arturjarosz.task.project.model.StageType;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntryApplicationService;

@ApplicationService
public class StageTypeApplicationServiceImpl extends AbstractNamedEntryApplicationService<StageType, StageTypeDto>
        implements StageTypeApplicationService {

    private final StageTypeMapper stageTypeMapper;

    public StageTypeApplicationServiceImpl(StageTypeRepository stageTypeRepository, StageTypeValidator stageTypeValidator, StageTypeMapper stageTypeMapper) {
        super(stageTypeRepository, stageTypeValidator, "Stage type");
        this.stageTypeMapper = stageTypeMapper;
    }

    @Override
    protected StageType createEntry(String name) {
        return new StageType(name);
    }

    @Override
    protected String nameOf(StageTypeDto stageTypeDto) {
        return stageTypeDto.getName();
    }

    @Override
    protected StageTypeDto toDto(StageType stageType, Long numberOfUsages) {
        return this.stageTypeMapper.mapToDto(stageType, numberOfUsages);
    }
}
