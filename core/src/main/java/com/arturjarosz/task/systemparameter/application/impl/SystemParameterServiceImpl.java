package com.arturjarosz.task.systemparameter.application.impl;

import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.systemparameter.application.SystemParameterService;
import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto;
import com.arturjarosz.task.systemparameter.infrastructure.repository.SystemParameterRepository;
import com.arturjarosz.task.systemparameter.model.SystemParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@ApplicationService
public class SystemParameterServiceImpl implements SystemParameterService {

    private final SystemParameterRepository systemParameterRepository;

    @Autowired
    public SystemParameterServiceImpl(SystemParameterRepository systemParameterRepository) {
        this.systemParameterRepository = systemParameterRepository;
    }

    @Transactional
    @Override
    public SystemParameterDto createSystemParameter(SystemParameterDto systemParameterDto,
                                                    SystemParameter systemParameter) {
        //TODO TA-198 - validate system parameter dto
        //TODO TA-198 - create new system parameter
        //TODO TA-198 - add system parameter
        this.systemParameterRepository.save(systemParameter);
        return null;
    }

    @Override
    public SystemParameterDto updateSystemParameter(Long systemParameterId, SystemParameterDto systemParameterDto) {
        //TODO TA-198 - implement updating system parameters - should it be possible all the time ?
        return null;
    }

    @Override
    public void removeSystemParameter(Long systemParameterId) {
        //TODO TA-198 - implement how to remove system parameters
    }

    @Override
    public SystemParameterDto getSystemParameter(Long systemParameterId) {
        //TODO TA-198 - implement retrieving system parameter
        return null;
    }

    @Override
    public List<SystemParameterDto> getSystemParameters() {
        //TODO TA-198 - retrieving all system parameters
        return null;
    }
}
