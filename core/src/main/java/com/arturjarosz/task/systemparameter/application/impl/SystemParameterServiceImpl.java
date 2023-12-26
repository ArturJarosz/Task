package com.arturjarosz.task.systemparameter.application.impl;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.systemparameter.application.SystemParameterApplicationValidator;
import com.arturjarosz.task.systemparameter.application.SystemParameterDtoMapper;
import com.arturjarosz.task.systemparameter.application.SystemParameterService;
import com.arturjarosz.task.systemparameter.domain.SystemParameterValidatorService;
import com.arturjarosz.task.systemparameter.infrastructure.repository.SystemParameterRepository;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@ApplicationService
public class SystemParameterServiceImpl implements SystemParameterService {

    private final SystemParameterRepository systemParameterRepository;
    private final SystemParameterApplicationValidator systemParameterApplicationValidator;
    private final SystemParameterValidatorService systemParameterValidatorService;
    private final SystemParameterQueryService systemParameterQueryService;

    @Autowired
    public SystemParameterServiceImpl(SystemParameterRepository systemParameterRepository,
            SystemParameterApplicationValidator systemParameterApplicationValidator,
            SystemParameterValidatorService systemParameterValidatorService,
            SystemParameterQueryService systemParameterQueryService) {
        this.systemParameterRepository = systemParameterRepository;
        this.systemParameterApplicationValidator = systemParameterApplicationValidator;
        this.systemParameterValidatorService = systemParameterValidatorService;
        this.systemParameterQueryService = systemParameterQueryService;
    }

    @Transactional
    @Override
    public SystemParameterDto updateSystemParameter(Long systemParameterId, SystemParameterDto systemParameterDto) {
        var maybeSystemParameter = this.systemParameterRepository.findById(systemParameterId);
        this.systemParameterApplicationValidator.validateParameterExistence(maybeSystemParameter,
                systemParameterDto.getName());
        this.systemParameterValidatorService.validateOnUpdate(systemParameterDto);
        var systemParameter = maybeSystemParameter.orElseThrow(ResourceNotFoundException::new);
        systemParameter.update(systemParameterDto);
        return SystemParameterDtoMapper.MAPPER.systemParameterToSystemParameterDto(systemParameter);
    }

    @Override
    public SystemParameterDto getSystemParameter(Long systemParameterId) {
        var systemParameterDto = this.systemParameterQueryService.getSystemParameter(systemParameterId);
        this.systemParameterApplicationValidator.validateParameterExistence(systemParameterDto, systemParameterId);
        return systemParameterDto;
    }

    @Override
    public List<SystemParameterDto> getSystemParameters() {
        return this.systemParameterQueryService.getSystemParameters();
    }
}
