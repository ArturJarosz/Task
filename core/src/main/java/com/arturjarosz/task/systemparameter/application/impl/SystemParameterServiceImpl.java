package com.arturjarosz.task.systemparameter.application.impl;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.systemparameter.application.SystemParameterApplicationValidator;
import com.arturjarosz.task.systemparameter.application.SystemParameterChangeListener;
import com.arturjarosz.task.systemparameter.application.SystemParameterDtoMapper;
import com.arturjarosz.task.systemparameter.application.SystemParameterService;
import com.arturjarosz.task.systemparameter.domain.SystemParameterValidatorService;
import com.arturjarosz.task.systemparameter.infrastructure.repository.SystemParameterRepository;
import com.arturjarosz.task.systemparameter.model.SystemParameterType;
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
    private final List<SystemParameterChangeListener> systemParameterChangeListeners;

    @Autowired
    public SystemParameterServiceImpl(SystemParameterRepository systemParameterRepository,
            SystemParameterApplicationValidator systemParameterApplicationValidator,
            SystemParameterValidatorService systemParameterValidatorService,
            SystemParameterQueryService systemParameterQueryService,
            List<SystemParameterChangeListener> systemParameterChangeListeners) {
        this.systemParameterRepository = systemParameterRepository;
        this.systemParameterApplicationValidator = systemParameterApplicationValidator;
        this.systemParameterValidatorService = systemParameterValidatorService;
        this.systemParameterQueryService = systemParameterQueryService;
        this.systemParameterChangeListeners = systemParameterChangeListeners;
    }

    @Transactional
    @Override
    public SystemParameterDto updateSystemParameter(Long systemParameterId, SystemParameterDto systemParameterDto) {
        var maybeSystemParameter = this.systemParameterRepository.findById(systemParameterId);
        this.systemParameterApplicationValidator.validateParameterExistence(maybeSystemParameter,
                systemParameterDto.getName());
        var systemParameter = maybeSystemParameter.orElseThrow(ResourceNotFoundException::new);
        // the stored name decides which validator runs, so a request cannot skip validation by sending another name
        systemParameterDto.setName(systemParameter.getName());
        var previousValue = systemParameter.getValue();
        // secret values are never sent to the client, so a missing value means "keep the stored one" and a blank one
        // removes it
        if (systemParameter.getType() == SystemParameterType.SECRET) {
            systemParameterDto.setValue(systemParameterDto.getValue() == null ? previousValue
                    : systemParameterDto.getValue().strip());
        }
        this.systemParameterValidatorService.validateOnUpdate(systemParameterDto);
        systemParameter.update(systemParameterDto);
        this.systemParameterChangeListeners.forEach(listener -> listener.onSystemParameterChanged(
                systemParameter.getName(), previousValue, systemParameter.getValue()));
        return SystemParameterDtoMapper.hideSecretValue(
                SystemParameterDtoMapper.MAPPER.systemParameterToSystemParameterDto(systemParameter));
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
